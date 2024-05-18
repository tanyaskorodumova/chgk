package com.itmo.chgk.security;

import java.util.List;

public class UserConverter {
    public static User convertRequestToUser(UserRequest request){
        User user = new User(request.getUsername(), request.getPassword());
        user.setEnabled(true);

        List<String> list = request.getAuthorities();
        for(String str:list){
            Authority auth = new Authority(str);
            user.addAuthority(auth);
            auth.setUsername(user);
        }

        return user;
    }
}
