    package com.uninote.backend.dto;

    import java.util.Set;

    public class CourseDTO {
        private Long id;
        private Long departmentId;
        private String code;
        private int semester;
        private Set<CourseNameDTO> courseNames;

        public CourseDTO() {
        }

        public CourseDTO(Long id, Long departmentId, String code, int semester, Set<CourseNameDTO> courseNames) {
            this.id = id;
            this.departmentId = departmentId;
            this.code = code;
            this.semester = semester;
            this.courseNames = courseNames;
        }
        

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getDepartmentId() {
            return departmentId;
        }

        public void setDepartmentId(Long departmentId) {
            this.departmentId = departmentId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public int getSemester() {
            return semester;
        }

        public void setSemester(int semester) {
            this.semester = semester;
        }

        public Set<CourseNameDTO> getCourseNames() {
            return courseNames;
        }

        public void setCourseNames(Set<CourseNameDTO> courseNames) {
            this.courseNames = courseNames;
        }
    }
