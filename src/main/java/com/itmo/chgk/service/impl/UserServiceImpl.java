package com.itmo.chgk.service.impl;

import com.itmo.chgk.exceptions.CustomException;
import com.itmo.chgk.model.db.entity.Authority;
import com.itmo.chgk.model.db.entity.User;
import com.itmo.chgk.model.db.repository.AuthorityRepository;
import com.itmo.chgk.model.db.repository.UserRepository;
import com.itmo.chgk.model.dto.request.UserRequest;
import com.itmo.chgk.model.dto.response.JwtAuthenticationResponse;
import com.itmo.chgk.service.UserService;
import com.itmo.chgk.utils.UserConverter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder encoder;
    private final JWTService jwtService;


    @Transactional
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    @Override
    public User getUser(String user) {
        if(user.isEmpty())
            throw new CustomException("Нельзя пустого имени пользователя", HttpStatus.BAD_REQUEST);

        Optional<User> OpEmp = userRepository.findById(user);
        if(OpEmp.isEmpty())
            throw new CustomException("Нет такого пользователя", HttpStatus.NOT_FOUND);

        return OpEmp.get();
    }


    @Transactional
    @Override
    public JwtAuthenticationResponse createUser(UserRequest request) {
        Optional<User> OpEmp = userRepository.findById(request.getUsername());
        if(OpEmp.isPresent())
            throw new CustomException("Пользователь с таким логином уже зарегистрирован", HttpStatus.BAD_REQUEST);

        User user = UserConverter.convertRequestToUser(request);
        String pass = user.getPassword();
        user.setPassword(encoder.encode(pass));

        userRepository.save(user);

        List <Authority> list = user.getAuthorities();
        authorityRepository.saveAll(list);

        String jwt = jwtService.generateToken(request.getUsername());
        String jwtR = jwtService.generateRToken(request.getUsername());

        return new JwtAuthenticationResponse(jwt, jwtR);
    }


    @Transactional
    @Override
    public User updateUser(UserRequest request) {
        User oldUser = getUser(request.getUsername());

        String pass = request.getPassword();
            if(pass.isEmpty()) {
                throw new CustomException("Пустой пароль", HttpStatus.BAD_REQUEST);
            }

        String CodPass = encoder.encode(pass);
        oldUser.setPassword(CodPass);

        authorityRepository.deleteAllByUsername(oldUser);
        authorityRepository.flush();
        oldUser.setAuthorities(new ArrayList<>());

        userRepository.save(oldUser);

        for(String str: request.getAuthorities()){
            Authority auth = new Authority(str);
            oldUser.addAuthority(auth);
            auth.setUsername(oldUser);
            authorityRepository.save(auth);
        }

        return userRepository.save(oldUser);
    }


    @Transactional
    @Override
    public String deleteUser(String user) {
        User oldUser = getUser(user);
        authorityRepository.deleteAll(oldUser.getAuthorities());
        userRepository.delete(getUser(user));
        return ("Пользователь " + user + " удален");
    }

    @Transactional
    public void updateAuthority (String username, List <String> newAuthority){
        User user = getUser(username);

        authorityRepository.deleteAllByUsername(user);
        authorityRepository.flush();
        user.setAuthorities(new ArrayList<>());

        userRepository.save(user);

        for(String str: newAuthority){
            Authority auth = new Authority(str);
            user.addAuthority(auth);
            auth.setUsername(user);
            authorityRepository.save(auth);
        }

        userRepository.save(user);
    }

    @Transactional
    public void addAuthority (String username, List <String> newAuthority){
        User user = getUser(username);
        List <String> userAuthorities = user.getAuthoritiesString();

        for(String str: newAuthority){
            if (!userAuthorities.contains(str)){
                Authority auth = new Authority(str);
                user.addAuthority(auth);
                auth.setUsername(user);
                authorityRepository.save(auth);
            }
        }
        userRepository.save(user);
    }

    @Transactional
    public void deleteAuthority (String username, String delAuthority){
        User user = getUser(username);
        List<Authority> list = user.getAuthorities();
        int number = -1;


        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAuthority().equals(delAuthority)) {
                number = i;
                break;
            }
        }

        if (number == -1)
            return;

        Authority delAuth = list.get(number);
        authorityRepository.delete(delAuth);
        list.remove(number);
        userRepository.save(user);
    }
}
