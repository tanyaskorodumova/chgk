package com.itmo.chgk.security;

import com.itmo.chgk.exceptions.CustomException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
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
//    private final PasswordEncoder encoder;


    @Transactional
    @Override
    public List<User> getAllUsers() {
        List <User> list = userRepository.findAll();
        return list;
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
            throw new CustomException("Есть такой пользователь", HttpStatus.BAD_REQUEST);

        User user = UserConverter.convertRequestToUser(request);
        String pass = user.getPassword();
//        user.setPassword(encoder.encode(pass));

        userRepository.save(user);

        List <Authority> list = user.getAuthorities();
        authorityRepository.saveAll(list);

//        String jwt = jwtService.generateToken(request.getUsername());
//        String jwtR = jwtService.generateRToken(request.getUsername());

        return new JwtAuthenticationResponse("Ф", "Й");
    }


    @Transactional
    @Override
    public User updateUser(UserRequest request) {
        User oldUser = getUser(request.getUsername());

        String pass = request.getPassword();
            if(pass.isEmpty()) {
                throw new CustomException("Нельзя пустой пароль", HttpStatus.BAD_REQUEST);
            }

//        String CodPass = encoder.encode(pass);
        oldUser.setPassword(pass);

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
    public String delete(String user) {
        User oldUser = getUser(user);
        authorityRepository.deleteAll(oldUser.getAuthorities());

        userRepository.delete(getUser(user));
        return ("Пользователь " + user + " удален");
    }
}
