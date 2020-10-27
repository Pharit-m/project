package com.example.project.service;

import com.example.project.model.entity.User;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class UserService {

    public User register (User org) throws Exception {
        User.UserLevel levelFromSalary = getLevelFromSalary(org.getSalary());
        String refCode = generateRefCode(org.getPhoneNumber(), new Date());

        User user = new User();
        user.setPassword(org.getPassword());
        user.setName(org.getName());
        user.setPhoneNumber(org.getPhoneNumber());
        user.setUserLevel(org.getUserLevel());
        user.setSalary(org.getSalary());
        user.setRefCode(refCode);
        user.setLevel(levelFromSalary);
        return user;
    }

    public String generateRefCode (String phoneNumber, Date date) throws Exception {
        String pattern = "yyyyMMdd";
        DateFormat dateFormat = new SimpleDateFormat(pattern);
        String format = dateFormat.format(date);
        String subPhoneNumber = phoneNumber.substring(phoneNumber.length() - 4);
        return format + subPhoneNumber;
    }

    public User.UserLevel getLevelFromSalary (Integer salary) throws Exception {
        if (greaterThan(salary, 50000)) {
            return User.UserLevel.PLATINUM;
        } else if (between(salary, 30000, 50000)) {
            return User.UserLevel.GOLD;
        } else if (lessThan(salary, 3000)){
            return User.UserLevel.SILVER;
        } else {
            throw new Exception("Salary does not pass the minimum");
        }
    }

    private static boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        return i >= minValueInclusive && i <= maxValueInclusive;
    }

    private static boolean greaterThan(int i, int valueInclusive) {
        return i > valueInclusive;
    }

    private static boolean lessThan(int i, int valueInclusive) {
        return i < valueInclusive;
    }
}
