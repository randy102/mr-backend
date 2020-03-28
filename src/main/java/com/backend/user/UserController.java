package com.backend.user;

import com.backend.security.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody LoginDTO input) throws Exception {

        UserEntity existedUser = userRepository.findByUsername(input.getUsername());

        if (existedUser == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not found: User");

        String hashedPassword = HashService.hash(input.getPassword());

        if (!existedUser.getPassword().equals(hashedPassword))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Incorrect Password");

        final UserDetails userDetails = userDetailsService.loadUserByUsername(input.getUsername());

        return jwtUtil.sign(userDetails);
    }

    @PutMapping("/register")
    public UserEntity createUser(@RequestBody CreateUserDto user) throws NoSuchAlgorithmException {
        String hashedPassword = HashService.hash(user.getPassword());
        Set<RoleEntity> roles = new HashSet<>();

        roles.add(new RoleEntity(RoleEnum.ROLE_USER.toString()));

        if (user.isAdmin())
            roles.add(new RoleEntity(RoleEnum.ROLE_ADMIN.toString()));

        return userRepository.save(new UserEntity(user.getUsername(), hashedPassword, roles, user.getImg()));
    }
}
