package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.db.entity.UserInfo;
import com.itmo.chgk.model.db.repository.QuestionRepo;
import com.itmo.chgk.model.db.repository.UserRepo;
import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.enums.QuestionStatus;
import com.itmo.chgk.service.QuestionService;
import com.itmo.chgk.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionServiceImpl implements QuestionService {
    private final ObjectMapper mapper;
    private final QuestionRepo questionRepo;
    private final UserRepo userRepo;

    @Override
    public Page<QuestionInfoResponse> getAllQuestions(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<QuestionInfoResponse> all = questionRepo.findAllByStatus(request, QuestionStatus.APPROVED)
                .getContent()
                .stream()
                .map(question -> {
                    QuestionInfoResponse questionInfoResponse = mapper.convertValue(question, QuestionInfoResponse.class);
                    questionInfoResponse.setAnswer("Скрыто");
                    questionInfoResponse.setSource("Скрыто");
                    return questionInfoResponse;
                })
                .collect(Collectors.toList());

        return new PageImpl<>(all);
    }

    @Override
    public Question getQuestionDb(Long id) {
        return questionRepo.findById(id).orElseThrow(() -> new CustomException("Вопрос не найден", HttpStatus.NOT_FOUND));
    }

    @Override
    public QuestionInfoResponse getQuestion(Long id) {
        Question question = getQuestionDb(id);
        QuestionInfoResponse questionInfoResponse = mapper.convertValue(question, QuestionInfoResponse.class);
        questionInfoResponse.setAnswer("Скрыто");
        questionInfoResponse.setSource("Скрыто");
        return questionInfoResponse;
    }

    @Override
    public List<QuestionInfoResponse> getQuestionPack(QuestionPackRequest request) {
        List<QuestionInfoResponse> questions = questionRepo.findByQuestionPackRequest(request.getMinComplexity() == null? 0 : request.getMinComplexity().ordinal(),
                request.getMaxComplexity() == null ? 5 : request.getMaxComplexity().ordinal(),
                request.getNumber() == null ? 10 : request.getNumber())
                .stream()
                .map(question -> {
                    QuestionInfoResponse questionInfoResponse = mapper.convertValue(question, QuestionInfoResponse.class);
                    questionInfoResponse.setAnswer("Скрыто");
                    questionInfoResponse.setSource("Скрыто");
                    return questionInfoResponse;
                })
                .collect(Collectors.toList());

        return questions;
    }

    @Override
    public QuestionInfoResponse getAnswer(Long id) {
        Question question = getQuestionDb(id);
        return mapper.convertValue(question, QuestionInfoResponse.class);
    }

    @Override
    public QuestionInfoResponse createQuestion(QuestionInfoRequest request) {
        if (request.getQuestion().isEmpty()) {
            throw new CustomException("Поле вопрос не может быть пустым", HttpStatus.BAD_REQUEST);
        }
        if (request.getAnswer().isEmpty()) {
            throw new CustomException("Поле ответ не может быть пустым", HttpStatus.BAD_REQUEST);
        }

        Question question = mapper.convertValue(request, Question.class);
        question.setStatus(QuestionStatus.NEW);
        question.setCreatedAt(LocalDateTime.now());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        UserInfo UI = userRepo.findById(user.getUsername()).get().getUserInfo();
        question.setUserInfo(UI);

        question = questionRepo.save(question);


        return mapper.convertValue(question, QuestionInfoResponse.class);
    }

    @Override
    public QuestionInfoResponse updateQuestion(Long id, QuestionInfoRequest request) {
        Question question = getQuestionDb(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userName = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            UserInfo questionMaker = question.getUserInfo();
            if (!questionMaker.getLogin().getUsername().equals(userName)){
                throw new CustomException("Пользователь не имеет прав на изменение данного вопроса", HttpStatus.FORBIDDEN);
            }
        }

        question.setQuestion(request.getQuestion() == null ? question.getQuestion() : request.getQuestion());
        question.setAnswer(request.getAnswer() == null ? question.getAnswer() : request.getAnswer());
        question.setSource(request.getSource() == null ? question.getSource() : request.getSource());
        question.setComplexity(request.getComplexity() == null ? question.getComplexity() : request.getComplexity());

        question.setStatus(QuestionStatus.CHANGED);
        question.setUpdatedAt(LocalDateTime.now());
        question = questionRepo.save(question);

        return mapper.convertValue(question, QuestionInfoResponse.class);
    }

    @Override
    public Page<QuestionInfoResponse> getQuestionsToApprove(Integer page, Integer perPage, String sort, Sort.Direction order) {
        Pageable request = PaginationUtil.getPageRequest(page, perPage, sort, order);

        List<QuestionInfoResponse> allNew = questionRepo.findNew(request)
                .getContent()
                .stream()
                .map(question -> mapper.convertValue(question, QuestionInfoResponse.class))
                .collect(Collectors.toList());

        Page<QuestionInfoResponse> pageResponse = new PageImpl<>(allNew);

        return pageResponse;
    }

    @Override
    public QuestionInfoResponse approveQuestion(Long id, QuestionInfoRequest request, QuestionStatus status) {
        Question question = getQuestionDb(id);

        if (question.getComplexity() == null && request.getComplexity() == null && status.equals(QuestionStatus.APPROVED)) {
            throw new CustomException("Для утвержденных вопросов необходимо установить сложность", HttpStatus.BAD_REQUEST);
        }

        question.setQuestion(request.getQuestion() == null ? question.getQuestion() : request.getQuestion());
        question.setAnswer(request.getAnswer() == null ? question.getAnswer() : request.getAnswer());
        question.setSource(request.getSource() == null ? question.getSource() : request.getSource());
        question.setComplexity(request.getComplexity() == null ? question.getComplexity() : request.getComplexity());

        question.setStatus(status);
        question.setUpdatedAt(LocalDateTime.now());
        question = questionRepo.save(question);

        return mapper.convertValue(question, QuestionInfoResponse.class);
    }

    @Override
    public void deleteQuestion(Long id) {
        Question question = getQuestionDb(id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String userName = user.getUsername();
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        List<String> listAuthorities = authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        if (!listAuthorities.contains("ROLE_ADMIN")) {
            UserInfo questionMaker = question.getUserInfo();
            if (!questionMaker.getLogin().getUsername().equals(userName)){
                throw new CustomException("Пользователь не имеет прав на изменение данного вопроса", HttpStatus.FORBIDDEN);
            }
        }
        question.setStatus(QuestionStatus.DELETED);
        question.setUpdatedAt(LocalDateTime.now());
        questionRepo.save(question);
    }

    @Override
    public List<Question> getQuestionsToDelete() {
        List<Question> allOld = questionRepo.findOld();

        return allOld;
    }
}
