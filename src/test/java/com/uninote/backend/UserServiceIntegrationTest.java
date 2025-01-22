package com.uninote.backend;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.extension.ExtendWith;

import com.uninote.backend.dto.UserDTO;
import com.uninote.backend.entity.Department;
import com.uninote.backend.entity.Rank;
import com.uninote.backend.entity.Role;
import com.uninote.backend.entity.University;
import com.uninote.backend.entity.User;
import com.uninote.backend.repository.DepartmentRepository;
import com.uninote.backend.repository.RankRepository;
import com.uninote.backend.repository.RoleRepository;
import com.uninote.backend.repository.UniversityRepository;
import com.uninote.backend.repository.UserRepository;
import com.uninote.backend.service.UserService;

@ExtendWith(SpringExtension.class) // Use JUnit 5's extension
@SpringBootTest
@Transactional
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UniversityRepository universityRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private RoleRepository roleRepository;

    private Department testDepartment;
    private University testUniversity;
    private Rank testRank;
    private Role testRole;

    @BeforeEach
    public void setUp() {
        testUniversity = new University();
        testUniversity.setLocation("Athens");
        University u1 =  universityRepository.save(testUniversity);
        
        testDepartment = new Department();
        testDepartment.setUniversity(u1);
        testDepartment.setCode("sdds");
        departmentRepository.save(testDepartment);

        

        testRank = new Rank();
        testRank.setId(1L);  
        testRank.setMinScore(0L);
        testRank.setRankName("Rank 1");
        rankRepository.save(testRank);

        testRole = new Role();
        testRole.setName("User");
        roleRepository.save(testRole);
    }

    @Test
    public void testCreateUserSuccessfully() {
        
        UserDTO userDTO = new UserDTO();
        userDTO.setFirebaseUid("validFirebaseUid");
        userDTO.setName("Test User");
        userDTO.setDepartmentId(testDepartment.getId());
        userDTO.setUniversityId(testUniversity.getId());
        userDTO.setEmail("test@example.com");
        userDTO.setUsername("testUser");
        userDTO.setRoleId(testRole.getId());

       
        User createdUser = userService.createUser(userDTO);

        
        assertNotNull(createdUser);
        assertEquals("validFirebaseUid", createdUser.getFirebaseUid());
        assertEquals("Test User", createdUser.getName());
        assertEquals("test@example.com", createdUser.getEmail());
        assertEquals("testUser", createdUser.getUsername());
        assertEquals(testDepartment.getId(), createdUser.getDepartment().getId());
        assertEquals(testUniversity.getId(), createdUser.getUniversity().getId());
        assertEquals(testRole.getId(), createdUser.getRole().getId());
        assertEquals(testRank.getId(), createdUser.getRank().getId());

        
        assertNotNull(userRepository.findById(createdUser.getId()));
    }

    @Test
    public void testCreateUserThrowsExceptionForNullFirebaseUid() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirebaseUid(null);  
        userDTO.setName("Test User");
        userDTO.setDepartmentId(testDepartment.getId());
        userDTO.setUniversityId(testUniversity.getId());
        userDTO.setEmail("test@example.com");
        userDTO.setUsername("testUser");
        userDTO.setRoleId(testRole.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDTO);
        });

        assertEquals("Firebase UID must not be null or empty", exception.getMessage());
    }

    @Test
    public void testCreateUserThrowsExceptionForInvalidDepartmentId() {
        UserDTO userDTO = new UserDTO();
        userDTO.setFirebaseUid("validFirebaseUid");
        userDTO.setName("Test User");
        userDTO.setDepartmentId(999L);  
        userDTO.setUniversityId(testUniversity.getId());
        userDTO.setEmail("test@example.com");
        userDTO.setUsername("testUser");
        userDTO.setRoleId(testRole.getId());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDTO);
        });

        assertEquals("Invalid department ID: 999", exception.getMessage());
    }


    
}

