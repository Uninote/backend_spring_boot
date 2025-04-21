package com.uninote.backend.entity;

import javax.persistence.*;

@Entity
@Table(name = "SPACE_RESOURCES", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"SPACE_ID", "RESOURCE_ID"})
})
public class SpaceResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "SPACE_ID", nullable = false)
    private Space space;

    @ManyToOne
    @JoinColumn(name = "RESOURCE_ID", nullable = false)
    private Resource resource;

    public SpaceResource() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Space getSpace() {
        return space;
    }

    public void setSpace(Space space) {
        this.space = space;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        return "SpaceResource{" +
                "id=" + id +
                ", spaceId=" + (space != null ? space.getId() : null) +
                ", resourceId=" + (resource != null ? resource.getId() : null) +
                '}';
    }
}
