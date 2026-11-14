// src/app/app.routes.ts

import { Routes } from '@angular/router';
import { LoginComponent }        from './Pages/login/login.component';
import { LayoutComponent }       from './Pages/layout/layout.component';
import { DashboardComponent }    from './Pages/dashboard/dashboard.component';
import { PuntoVentaComponent }   from './Pages/punto-venta/punto-venta.component';
import { VentasComponent }       from './Pages/ventas/ventas.component';
import { DevolucionesComponent } from './Pages/devoluciones/devoluciones.component';
import { CajaComponent }         from './Pages/caja/caja.component';
import { ProductosComponent }    from './Pages/productos/productos.component';
import { CategoriasComponent }   from './Pages/categorias/categorias.component';
import { StockComponent }        from './Pages/stock/stock.component';
import { ComprasComponent }      from './Pages/compras/compras.component';
import { ProveedoresComponent }  from './Pages/proveedores/proveedores.component';
import { ClientesComponent }     from './Pages/clientes/clientes.component';
import { ReportesComponent }     from './Pages/reportes/reportes.component';
import { UsuariosComponent }     from './Pages/usuarios/usuarios.component';
import { ConfiguracionComponent } from './Pages/configuracion/configuracion.component';
import { MantenimientoComponent } from './Pages/mantenimiento/mantenimiento.component';
import { authGuard }             from './Guards/auth.guard';
import { rolGuard }              from './Guards/rol.guard';
import { MfaSetupComponent }     from './Pages/mfa-setup/mfa-setup.component';
import { MfaVerifyComponent }    from './Pages/mfa-verify/mfa-verify.component';
import { ChangePasswordComponent } from './Pages/change-password/change-password.component';
import { ForgotPasswordComponent } from './Pages/forgot-password/forgot-password.component';
import { ForgotVerifyComponent } from './Pages/forgot-verify/forgot-verify.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },

  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: 'mfa-setup',        component: MfaSetupComponent,       canActivate: [rolGuard], data: { titulo: 'Seguridad' } },
      { path: 'dashboard',        component: DashboardComponent,    canActivate: [rolGuard], data: { titulo: 'Dashboard' } },
      { path: 'punto-de-venta',   component: PuntoVentaComponent,   canActivate: [rolGuard], data: { titulo: 'Ventas' } },
      { path: 'historial-ventas', component: VentasComponent,       canActivate: [rolGuard], data: { titulo: 'Ventas' } },
      { path: 'devoluciones',     component: DevolucionesComponent, canActivate: [rolGuard], data: { titulo: 'Ventas' } },
      { path: 'caja',             component: CajaComponent,         canActivate: [rolGuard], data: { titulo: 'Caja' } },
      { path: 'productos',        component: ProductosComponent,    canActivate: [rolGuard], data: { titulo: 'Inventario' } },
      { path: 'categorias',       component: CategoriasComponent,   canActivate: [rolGuard], data: { titulo: 'Inventario' } },
      { path: 'stock',            component: StockComponent,        canActivate: [rolGuard], data: { titulo: 'Inventario' } },
      { path: 'compras',          component: ComprasComponent,      canActivate: [rolGuard], data: { titulo: 'Abastecimiento' } },
      { path: 'proveedores',      component: ProveedoresComponent,  canActivate: [rolGuard], data: { titulo: 'Abastecimiento' } },
      { path: 'clientes',         component: ClientesComponent,     canActivate: [rolGuard], data: { titulo: 'Clientes' } },
      { path: 'reportes',         component: ReportesComponent,     canActivate: [rolGuard], data: { titulo: 'Reportes' } },
      { path: 'usuarios',         component: UsuariosComponent,     canActivate: [rolGuard], data: { titulo: 'Administración' } },
      { path: 'configuracion',    component: ConfiguracionComponent, canActivate: [rolGuard], data: { titulo: 'Administración' } },
      { path: 'mantenimiento',    component: MantenimientoComponent, canActivate: [rolGuard], data: { titulo: 'Administración' } },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  { path: 'mfa-verify', component: MfaVerifyComponent },
  { path: 'change-password', component: ChangePasswordComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'forgot-verify', component: ForgotVerifyComponent },

  { path: '**', redirectTo: 'login' }
];
