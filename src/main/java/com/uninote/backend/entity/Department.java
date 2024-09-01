    package com.uninote.backend.entity;

    import javax.persistence.*;
    import java.util.HashSet;
    import java.util.Set;

    @Entity
    @Table(name = "departments")
    public class Department {

        @Id
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department_seq")
        @SequenceGenerator(name = "department_seq", sequenceName = "seq_department_id", allocationSize = 1)
        @Column(name = "department_id", nullable = false, updatable = false)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "university_id", nullable = false)
        private University university;


        @Column(name = "department_code", nullable = false)
        private String code;

        @Column(name = "number_of_semesters", nullable = false)
        private int semesters;

        @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
        private Set<DepartmentName> departmentNames = new HashSet<>();

        @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
        private Set<Course> courses = new HashSet<>();

        
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public University getUniversity() {
            return university;
        }

        public void setUniversity(University university) {
            this.university = university;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getmSemesters() {
            return semesters;
        }

        public void setNumSemesters(int numSemesters) {
            this.semesters = numSemesters;
        }

        public Set<DepartmentName> getDepartmentNames() {
            return departmentNames;
        }

        public void setDepartmentNames(Set<DepartmentName> departmentNames) {
            this.departmentNames = departmentNames;
        }

        public Set<Course> getCourses() {
            return courses;
        }

        public void setCourses(Set<Course> courses) {
            this.courses = courses;
        }
    }
