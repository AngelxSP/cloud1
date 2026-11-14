import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  email = '';
  message = '';
  loading = false;

  constructor(private auth: AuthService, private router: Router) {}

  enviar() {
    this.loading = true;
    this.auth.forgotPassword({ email: this.email }).subscribe({
      next: () => { this.message = 'Código enviado, revisa tu correo'; this.loading = false; this.router.navigate(['/forgot-verify'], { state: { email: this.email } }); },
      error: () => { this.message = 'Error enviando código'; this.loading = false; }
    });
  }
}
