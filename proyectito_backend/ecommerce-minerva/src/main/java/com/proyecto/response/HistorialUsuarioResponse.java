package com.proyecto.response;
import lombok.Data;
import java.util.List;

@Data
public class HistorialUsuarioResponse {
	private List<String> categoriasMasCompradas;
    private List<String> marcasPreferidas;
    private String generoUsuario; // Ej: "Hombre", "Mujer"
	public List<String> getCategoriasMasCompradas() {
		return categoriasMasCompradas;
	}
	public void setCategoriasMasCompradas(List<String> categoriasMasCompradas) {
		this.categoriasMasCompradas = categoriasMasCompradas;
	}
	public List<String> getMarcasPreferidas() {
		return marcasPreferidas;
	}
	public void setMarcasPreferidas(List<String> marcasPreferidas) {
		this.marcasPreferidas = marcasPreferidas;
	}
	public String getGeneroUsuario() {
		return generoUsuario;
	}
	public void setGeneroUsuario(String generoUsuario) {
		this.generoUsuario = generoUsuario;
	}
}
