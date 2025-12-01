import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastService } from '../services/toast.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private toastService: ToastService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        let errorMessage = 'Erro inesperado';
        let errorTitle = 'Erro';

        if (error.error instanceof ErrorEvent) {
          // Erro do lado do cliente
          errorMessage = error.error.message;
          errorTitle = 'Erro de Conexão';
        } else {
          // Erro do lado do servidor
          switch (error.status) {
            case 0:
              errorTitle = 'Sem Conexão';
              errorMessage = 'Não foi possível conectar ao servidor. Verifique sua conexão.';
              break;
            case 400:
              errorTitle = 'Dados Inválidos';
              errorMessage = error.error?.message || 'Os dados enviados são inválidos.';
              break;
            case 401:
              errorTitle = 'Não Autorizado';
              errorMessage = 'Sua sessão expirou. Faça login novamente.';
              break;
            case 403:
              errorTitle = 'Acesso Negado';
              errorMessage = 'Você não tem permissão para esta operação.';
              break;
            case 404:
              errorTitle = 'Não Encontrado';
              errorMessage = 'O recurso solicitado não foi encontrado.';
              break;
            case 500:
              errorTitle = 'Erro do Servidor';
              errorMessage = 'Erro interno do servidor. Tente novamente mais tarde.';
              break;
            case 502:
            case 503:
            case 504:
              errorTitle = 'Serviço Indisponível';
              errorMessage = 'O serviço está temporariamente indisponível. Tente novamente.';
              break;
            default:
              errorTitle = 'Erro HTTP';
              errorMessage = error.error?.message || `Erro ${error.status}: ${error.statusText}`;
          }
        }

        // Não mostrar toast para alguns endpoints específicos (se necessário)
        const skipToastUrls = ['/auth/userinfo']; // Adicione URLs que não devem mostrar toast
        const shouldShowToast = !skipToastUrls.some(url => req.url.includes(url));

        if (shouldShowToast) {
          this.toastService.showError(errorTitle, errorMessage);
        }

        return throwError(() => error);
      })
    );
  }
}