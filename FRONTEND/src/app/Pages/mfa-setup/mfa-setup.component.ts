import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-mfa-setup',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './mfa-setup.component.html',
  styleUrls: ['./mfa-setup.component.css']
})
export class MfaSetupComponent {
  secret = '';
  otpAuthUrl = '';
  code = '';
  message = '';
  loading = false;

  constructor(private auth: AuthService) {}

  generar() {
    this.loading = true;
    this.auth.setupMfa().subscribe({
      next: (res: any) => {
        this.secret = res.secret;
        this.otpAuthUrl = res.otpAuthUrl;
        this.loading = false;
      },
      error: (e) => { this.message = 'Error al generar secreto'; this.loading = false; }
    });
  }

  verificar() {
    this.loading = true;
    this.auth.enableMfa({ email: this.auth.getEmail() || '', method: 'totp', code: this.code }).subscribe({
      next: () => { this.message = 'MFA habilitado'; this.loading = false; },
      error: () => { this.message = 'Código inválido'; this.loading = false; }
    });
  }
}
