package com.proyecto.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
public class Categoria {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	@NotNull@Size(max=50)
	@Column(length=50)
	private String name;

	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="padre_categoria_id")
	private Categoria padrecategoria;

	private int level;

	public Categoria() {

	}

	public Categoria(Long id, String name, Categoria padrecategoria, int level) {
		super();
		this.id = id;
		this.name = name;
		this.padrecategoria = padrecategoria;
		this.level = level;
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

	public Categoria getPadrecategoria() {
		return padrecategoria;
	}

	public void setPadrecategoria(Categoria padrecategoria) {
		this.padrecategoria = padrecategoria;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

}
