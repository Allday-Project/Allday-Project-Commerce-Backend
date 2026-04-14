package jpa.basic.alldayprojectcommerce.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Email
    @Column(nullable = false, length = 100, unique = true)
    private String email;

    @Column
    private String password;

    @Column(length = 100)
    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    public static User createUser(String email, String encodedPassword) {
        User user = new User();
        user.email = email;
        user.password = encodedPassword;
        return user;
    }

    public void updateProfile(String name, String encodedPassword, String phone, String address) {
        if(name != null) this.name = name;
        this.password = encodedPassword;
        if(phone != null)this.phone = phone;
        if(address != null)this.address = address;
    }
}
