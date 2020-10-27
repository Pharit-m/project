package com.example.project;

import com.example.project.model.entity.User;
import com.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
public class AccessResource {

    @Autowired
    UserService userService;

    @RequestMapping({"/hello"})
    public String hello(){
        return "hello";
    }

    @RequestMapping(value = "/register/user", method = POST)
    @ResponseBody
    public User registerByEmail(@RequestBody User user) throws Exception {
        User userRegister = userService.register(user);
        return userRegister;
    }
}
