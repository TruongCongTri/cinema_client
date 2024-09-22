package com.example.cinema_client.models;

import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.util.Set;

/**
 * @author tritcse00526x
 */
@Data
public class User {
    private Integer id;

    @NotBlank(message = "Tên đăng nhập không được để trống!")
    @Email(message = "Tên đăng nhập không hợp lệ!")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống!")
    @Size(min =  6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    @NotBlank(message = "Họ tên không được để trống!")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống!")
    @Size(min = 9, max = 11, message = "Số điện thoại không hợp lệ!")
    private String phone;

    private Set<Role> roles;

}
