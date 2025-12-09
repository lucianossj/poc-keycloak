import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class CompleteProfileService {
  private readonly backendUrl = 'http://localhost:8081';

  constructor(private http: HttpClient) {}

  async updateCustomerInfo(
    keycloakUserId: string,
    document: string,
    birthDate: string
  ): Promise<any> {
    const payload = {
      document: document,
      birthDate: birthDate
    };

    return firstValueFrom(
      this.http.patch(
        `${this.backendUrl}/api/customers/update-info/${keycloakUserId}`,
        payload
      )
    );
  }
}
