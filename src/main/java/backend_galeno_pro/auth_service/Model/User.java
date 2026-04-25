package backend_galeno_pro.auth_service.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false,unique = true)
    @NotEmpty
    private String username;

    @Column(nullable = false)
    @NotEmpty
    private String password;

    @Column(name = "correo_institucional", unique = true)
    private String correoInstitucional;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado",nullable = false)
    @Builder.Default
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @NotEmpty
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento",length = 20)
    private TipoDocumento tipoDocumento;

    @Column(name = "numero_documento",unique = true,length = 20)
    @NotEmpty
    private String numeroDocumento;

    @Column(name = "nombres" ,nullable = false)
    @NotEmpty
    private String nombres;

    @Column(name = "apellido_paterno",nullable = false)
    @NotEmpty
    private String apellidoPaterno;

    @Column(name = "apellido_materno",nullable = false)
    @NotEmpty
    private String apellidoMaterno;

    @Column(name = "fecha_nacimiento")
    @NotEmpty
    private LocalDate fechaNacimiento;

    @Column(name = "telefono",nullable = false,length = 15)
    private String telefono;

    @Column(name = "direccionDomicilio")
    private String direccionDomicilio;

    @Column(name = "colegiatura",length = 50)
    private String colegiatura;

    @Enumerated(EnumType.STRING)
    @Column(name = "condicion_laboral")
    private CondicionLaboral condicionLaboral;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_asignada")
    private AreaAsignada areaAsignada;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    @Column(name = "fecha_cese")
    private LocalDate fechaCese;

    @Column(name = "firma_digital")
    private String firmaDigital;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"),inverseJoinColumns = @JoinColumn(name = "role_id"))

    private Set<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities(){
        return roles.stream().map(r -> new SimpleGrantedAuthority(r.getName().name())).collect(Collectors.toSet());
    }
    @Override
    public boolean isAccountNonExpired(){
        return true;
    }

    @Override
    public boolean isAccountNonLocked(){
        return estado == EstadoUsuario.ACTIVO || estado == EstadoUsuario.VACACIONES;
    }

    @Override
    public boolean isCredentialsNonExpired(){
        return true;
    }

    @Override
    public boolean isEnabled(){
        return estado == EstadoUsuario.ACTIVO;
    }


}
