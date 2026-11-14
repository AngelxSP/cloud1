import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../Services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-mfa-verify',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mfa-verify.component.html',
  styleUrls: ['./mfa-verify.component.css']
})
export class MfaVerifyComponent {
  method = 'totp';
  code = '';
  message = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  enviarEmailOtp() {
    this.loading = true;
    this.auth.sendEmailOtp({ email: this.auth.getEmail() || '' }).subscribe({
      next: () => { this.message = 'Código enviado por correo'; this.loading = false; },
      error: () => { this.message = 'Error al enviar correo'; this.loading = false; }
    });
  }

  verificar() {
    this.loading = true;
    this.auth.verifyMfa({ email: this.auth.getEmail() || '', method: this.method, code: this.code }).subscribe({
      next: () => { this.router.navigate(['/dashboard']); },
      error: () => { this.message = 'Código inválido'; this.loading = false; }
    });
  }
}
