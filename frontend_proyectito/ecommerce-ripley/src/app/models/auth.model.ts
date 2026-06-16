export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  firstName: string;
  lastName: string;
  email: string;
  password: string;
  celular?: string;
}

export interface AuthResponse {
  jwt: string;
  message: string;
}