package com.nicolas.authentication.dto.outgoing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String status;
    private String statusDesc;
    private String role;
    private String roleDesc;
    private String CreationDate;
    private String LastUpdate;
}
