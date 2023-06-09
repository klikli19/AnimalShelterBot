package pro.sky.animalshelterbot.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

import static javax.persistence.GenerationType.IDENTITY;

/**
 * Класс Shelter, представляет сущность приюта
 * @author Kilikova Anna
 */
@Entity
@Table(name = "shelter")
public class Shelter {

    /**
     * Поле: идентификационный номер приюта
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Поле: название приюта
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Поле: адрес приюта
     */
    @Column(name = "address", nullable = false)
    private String address;

    /**
     * Поле: номер телефона приюта
     */
    @Column(name = "phone", nullable = false)
    private String phone;

    /**
     * Поле: адрес электронной почты приюта
     */
    @Column(name = "mail", nullable = false)
    private String mail;

    /**
     * Поле: часы работы приюта
     */
    @Column(name = "opening_hours", nullable = false)
    private String openingHours;

    /**
     * Поле: схема расположения приюта на карте
     */
    @Column(name = "map", nullable = false)
    private byte[] map;

    public Shelter(Long id, String name, String address, String phone, String mail, String openingHours, byte[] map) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.mail = mail;
        this.openingHours = openingHours;
        this.map = map;
    }

    public Shelter() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public byte[] getMap() {
        return map;
    }

    public void setMap(byte[] map) {
        this.map = map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Shelter shelter = (Shelter) o;
        return Objects.equals(id, shelter.id) && Objects.equals(name, shelter.name) && Objects.equals(address, shelter.address) && Objects.equals(phone, shelter.phone) && Objects.equals(mail, shelter.mail) && Objects.equals(openingHours, shelter.openingHours) && Arrays.equals(map, shelter.map);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, name, address, phone, mail, openingHours);
        result = 31 * result + Arrays.hashCode(map);
        return result;
    }

    @Override
    public String toString() {
        return "Shelter{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", mail='" + mail + '\'' +
                ", openingHours=" + openingHours +
                ", map=" + Arrays.toString(map) +
                '}';
    }

}
