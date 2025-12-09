

import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { AuthCallbackComponent } from './auth-callback/auth-callback.component';
import { HomeComponent } from './home/home.component';
import { LogoutCallbackComponent } from './logout-callback/logout-callback.component';
import { CompleteProfileComponent } from './complete-profile/complete-profile.component';
import { AuthGuard } from './auth.guard';
import { LoginGuard } from './login.guard';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent, canActivate: [AuthGuard] },
  { path: 'login', component: LoginComponent, canActivate: [LoginGuard] },
  { path: 'auth/callback', component: AuthCallbackComponent },
  { path: 'complete-profile', component: CompleteProfileComponent, canActivate: [AuthGuard] },
  { path: 'logout-callback', component: LogoutCallbackComponent },
];

export { routes };