import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../Services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './change-password.component.html',
  styleUrls: ['./change-password.component.css']
})
export class ChangePasswordComponent {
  oldPassword = '';
  newPassword = '';
  message = '';
  loading = false;

  constructor(private auth: AuthService) {}

  cambiar() {
    this.loading = true;
    this.auth.changePassword({ email: this.auth.getEmail() || '', oldPassword: this.oldPassword, newPassword: this.newPassword }).subscribe({
      next: () => { this.message = 'Contraseña actualizada'; this.loading = false; },
      error: () => { this.message = 'Error al cambiar contraseña'; this.loading = false; }
    });
  }
}
