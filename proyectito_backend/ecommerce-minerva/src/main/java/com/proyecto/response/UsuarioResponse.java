package com.proyecto.response;

import java.time.LocalDate;

import com.proyecto.model.Usuario;

public class UsuarioResponse {
    private String email;
    private String firstName;
    private String lastName;
    private String direccion;
    private LocalDate fechaNacimiento;
    private String celular;
    private String foto;
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public LocalDate getFechaNacimiento() {
		return fechaNacimiento;
	}
	public void setFechaNacimiento(LocalDate fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}
	public String getCelular() {
		return celular;
	}
	public void setCelular(String celular) {
		this.celular = celular;
	}
	public String getFoto() {
		return foto;
	}
	public void setFoto(String foto) {
		this.foto = foto;
	}



	public UsuarioResponse(String email, String firstName, String lastName, String direccion,
			LocalDate fechaNacimiento, String celular, String foto) {
		super();
		this.email = email;
		this.firstName = firstName;
		this.lastName = lastName;
		this.direccion = direccion;
		this.fechaNacimiento = fechaNacimiento;
		this.celular = celular;
		this.foto = foto;
	}
	public UsuarioResponse(Usuario usuario) {
	    this.email = usuario.getEmail();
	    this.firstName = usuario.getFirstName();
	    this.lastName = usuario.getLastName();
	    this.direccion = usuario.getDireccion();
	    this.fechaNacimiento = usuario.getFechaNacimiento();
	    this.celular = usuario.getCelular();
	    this.foto = usuario.getFoto();
	}
}