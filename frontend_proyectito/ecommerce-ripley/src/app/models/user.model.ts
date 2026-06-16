export interface UsuarioResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  celular?: string;
  direccion?: string;
  fechaNacimiento?: string;
  foto?: string;
}

export interface AdminUsuarioResponse {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  active: boolean;
  createdAt: string;
}

export interface AdminCreateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  password?: string;
  role: string;
  celular?: string;
}

export interface AdminUpdateUserRequest {
  firstName: string;
  lastName: string;
  email: string;
  role: string;
  password?: string;
}