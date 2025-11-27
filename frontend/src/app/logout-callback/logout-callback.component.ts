import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-logout-callback',
  template: `<p>Logout realizado. Redirecionando...</p>`
})
export class LogoutCallbackComponent implements OnInit {
  constructor(private router: Router) {}
  ngOnInit() {
    setTimeout(() => this.router.navigate(['/login']), 1500);
  }
}
