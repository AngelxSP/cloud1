// src/app/Services/auth.service.ts

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { appsettings } from '../Settings/appsettings';

interface LoginRequest {
  email: string;
  password: string;
}

interface LoginResponse {
  token: string;
  nombre: string;
  email: string;
  rol: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private apiUrl = `${appsettings.apiUrl}/auth`;

  constructor(private http: HttpClient) {}

  login(datos: LoginRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/login`, datos).pipe(
      tap((res) => {
        // Only store values if backend returned a token (no MFA challenge)
        if (res?.token) {
          localStorage.setItem('token', res.token);
          localStorage.setItem('nombre', res.nombre);
          localStorage.setItem('email', res.email);
          localStorage.setItem('rol', res.rol);
          const id = this.decodeUsuarioId(res.token);
          if (id != null) localStorage.setItem('usuarioId', String(id));
        }
      })
    );
  }

  getEmail(): string | null {
    return localStorage.getItem('email');
  }

  setupMfa(): Observable<any> {
    const token = localStorage.getItem('token');
    return this.http.post<any>(`${this.apiUrl}/setup-mfa`, { email: localStorage.getItem('email') }, { headers: { Authorization: token ? 'Bearer ' + token : '' } });
  }

  forgotPassword(body: { email: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/forgot-password`, body);
  }

  verifyForgotPassword(body: { email: string; code: string; newPassword: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/verify-forgot-password`, body);
  }

  enableMfa(body: { email: string; method: string; code: string }): Observable<any> {
    const token = localStorage.getItem('token');
    return this.http.post<any>(`${this.apiUrl}/enable-mfa`, body, { headers: { Authorization: token ? 'Bearer ' + token : '' } });
  }

  sendEmailOtp(body: { email: string }): Observable<any> {
    const token = localStorage.getItem('token');
    return this.http.post<any>(`${this.apiUrl}/send-email-otp`, body, { headers: { Authorization: token ? 'Bearer ' + token : '' } });
  }

  verifyMfa(body: { email: string; method: string; code: string }): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/verify-mfa`, body).pipe(
      tap((res: any) => {
        if (res?.token) {
          localStorage.setItem('token', res.token);
        }
      })
    );
  }

  changePassword(body: { email: string; oldPassword: string; newPassword: string }): Observable<any> {
    const token = localStorage.getItem('token');
    return this.http.post<any>(`${this.apiUrl}/change-password`, body, { headers: { Authorization: token ? 'Bearer ' + token : '' } });
  }

  logout(): void {
    localStorage.removeItem('token');
    localStorage.removeItem('nombre');
    localStorage.removeItem('rol');
    localStorage.removeItem('usuarioId');
  }

  estaAutenticado(): boolean {
    return !!localStorage.getItem('token');
  }

  getRol(): string | null {
    return localStorage.getItem('rol');
  }

  getNombre(): string | null {
    return localStorage.getItem('nombre');
  }

  /** Id del usuario logueado (claim "usuarioId" del JWT). */
  getUsuarioId(): number {
    const guardado = localStorage.getItem('usuarioId');
    if (guardado) return Number(guardado);
    const token = localStorage.getItem('token');
    const id = token ? this.decodeUsuarioId(token) : null;
    return id != null ? id : 0;
  }

  private decodeUsuarioId(token: string): number | null {
    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      return payload.usuarioId ?? null;
    } catch {
      return null;
    }
  }
}
