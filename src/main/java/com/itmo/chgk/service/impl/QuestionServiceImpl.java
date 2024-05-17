package com.itmo.chgk.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Question;
import com.itmo.chgk.model.db.repository.QuestionRepo;
import com.itmo.chgk.model.dto.request.QuestionInfoRequest;
import com.itmo.chgk.model.dto.request.QuestionPackRequest;
import com.itmo.chgk.model.dto.response.QuestionInfoResponse;
import com.itmo.chgk.model.enums.QuestionStatus;
import com.itmo.chgk.model.enums.UserRole;
import com.itmo.chgk.service.QuestionService;
import com.itmo.chgk.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionServiceImpl implements QuestionService {
    private final ObjectMapper mapper;
    private final QuestionRepo questionRepo;
//    private final LoggedUserManagementService loggedUserManagementService;

    @Override
    public Page<QuestionInfoResponse> getAllQuestions(Integer page, Integer perPage, String sort, Sort.Direction order) {
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        }

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
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        }

        Question question = getQuestionDb(id);
        QuestionInfoResponse questionInfoResponse = mapper.convertValue(question, QuestionInfoResponse.class);
        questionInfoResponse.setAnswer("Скрыто");
        questionInfoResponse.setSource("Скрыто");
        return questionInfoResponse;
    }

    @Override
    public List<QuestionInfoResponse> getQuestionPack(QuestionPackRequest request) {
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        }

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
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        }

        Question question = getQuestionDb(id);
        return mapper.convertValue(question, QuestionInfoResponse.class);
    }

    @Override
    public QuestionInfoResponse createQuestion(QuestionInfoRequest request) {
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        }

        if (request.getQuestion().isEmpty()) {
            throw new CustomException("Поле вопрос не может быть пустым", HttpStatus.BAD_REQUEST);
        }
        if (request.getAnswer().isEmpty()) {
            throw new CustomException("Поле ответ не может быть пустым", HttpStatus.BAD_REQUEST);
        }

        Question question = mapper.convertValue(request, Question.class);
        question.setStatus(QuestionStatus.NEW);
        question.setCreatedAt(LocalDateTime.now());
//        question.setUserInfo(loggedUserManagementService.getUserInfo());
        question = questionRepo.save(question);

        return mapper.convertValue(question, QuestionInfoResponse.class);

    }

    @Override
    public QuestionInfoResponse updateQuestion(Long id, QuestionInfoRequest request) {
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        }

        Question question = getQuestionDb(id);

//        if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !question.getUserInfo().getId().equals(loggedUserManagementService.getUserInfo().getId())) {
//            throw new CustomException("Пользователь не имеет прав на редактирование данного вопроса", HttpStatus.FORBIDDEN);
//        }

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
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN)) {
//            throw new CustomException("Необходимо обладать правами администратора", HttpStatus.FORBIDDEN);
//        }

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
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        } else if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN)) {
//            throw new CustomException("Необходимо обладать правами администратора", HttpStatus.FORBIDDEN);
//        }

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
//        if (loggedUserManagementService.getUserInfo() == null) {
//            throw new CustomException("Необходимо авторизоваться", HttpStatus.UNAUTHORIZED);
//        }

        Question question = getQuestionDb(id);

//        if (!loggedUserManagementService.getUserInfo().getRole().equals(UserRole.ADMIN) &&
//                !question.getUserInfo().getId().equals(loggedUserManagementService.getUserInfo().getId())) {
//            throw new CustomException("Пользователь не имеет прав на удаление данного вопроса", HttpStatus.FORBIDDEN);
//        }

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
