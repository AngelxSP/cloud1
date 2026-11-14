import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-forgot-verify',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-verify.component.html',
  styleUrls: ['./forgot-verify.component.css']
})
export class ForgotVerifyComponent {
  email = '';
  code = '';
  newPassword = '';
  message = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {
    const state: any = this.router.getCurrentNavigation()?.extras?.state;
    if (state?.email) this.email = state.email;
  }

  verificar() {
    this.loading = true;
    this.auth.verifyForgotPassword({ email: this.email, code: this.code, newPassword: this.newPassword }).subscribe({
      next: () => { this.message = 'Contraseña restablecida. Puedes iniciar sesión.'; this.loading = false; this.router.navigate(['/login']); },
      error: () => { this.message = 'Código inválido o error'; this.loading = false; }
    });
  }
}
