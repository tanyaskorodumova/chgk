package com.itmo.chgk.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RestController
@RequestMapping("/users")
public class UserController {

        UserService userService;

        @GetMapping("/all")
        public List<User> getAllUsers(){
            return userService.getAllUsers();
        }

        @GetMapping("/{user}")
        public User getUser(@PathVariable String user){
            return userService.getUser(user);
        }

        @PostMapping("/new")
        public JwtAuthenticationResponse createUser(@RequestBody UserRequest request){
            return userService.createUser(request);
        }

        @PutMapping("/")
        public User updateUser(@RequestBody UserRequest request){
            return userService.updateUser(request);
        }

        @DeleteMapping("/{user}")
        public String deleteUser(@PathVariable String user){
            return userService.delete(user);
        }
}
