package jpa.basic.alldayprojectcommerce.domain.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jpa.basic.alldayprojectcommerce.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String name;

    @Email
    @Column(nullable = false, length = 100)
    private String email;

    @Column(length = 100)
    private String password;

    @Column(length = 100)
    private String phone;

    private String address;


}
