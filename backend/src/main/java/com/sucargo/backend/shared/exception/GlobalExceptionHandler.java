package com.sucargo.backend.shared.exception;

import com.sucargo.backend.auth.service.CredencialesInvalidasException;
import com.sucargo.backend.auth.service.TokenInvalidoException;
import com.sucargo.backend.auth.service.UsuarioInactivoException;
import com.sucargo.backend.cliente.service.ClienteNoEncontradoException;
import com.sucargo.backend.cliente.service.PuntoEntregaNoEncontradoException;
import com.sucargo.backend.cliente.service.RucDniYaExisteException;
import com.sucargo.backend.inventario.service.InventarioNoEncontradoException;
import com.sucargo.backend.inventario.service.MovimientoInvalidoException;
import com.sucargo.backend.inventario.service.RolNoAutorizadoException;
import com.sucargo.backend.pedido.service.PedidoInvalidoException;
import com.sucargo.backend.pedido.service.PedidoNoCancelableException;
import com.sucargo.backend.pedido.service.PedidoNoEncontradoException;
import com.sucargo.backend.picking.service.PickingInvalidoException;
import com.sucargo.backend.picking.service.PickingNoEncontradoException;
import com.sucargo.backend.producto.service.CategoriaNoEncontradaException;
import com.sucargo.backend.producto.service.ProductoNoEncontradoException;
import com.sucargo.backend.producto.service.SkuYaExisteException;
import com.sucargo.backend.usuario.service.RolNoPermitidoException;
import com.sucargo.backend.usuario.service.UsuarioNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.sucargo.backend.packing.service.BultoInvalidoException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import com.sucargo.backend.packing.service.BultoNoEncontradoException;
import com.sucargo.backend.distribucion.service.DistribucionInvalidaException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Object> handleCredencialesInvalidas(
            CredencialesInvalidasException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<Object> handleUsuarioInactivo(
            UsuarioInactivoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    @ExceptionHandler(TokenInvalidoException.class)
    public ResponseEntity<Object> handleTokenInvalido(
            TokenInvalidoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(RucDniYaExisteException.class)
    public ResponseEntity<Object> handleRucDniYaExiste(
            RucDniYaExisteException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ClienteNoEncontradoException.class)
    public ResponseEntity<Object> handleClienteNoEncontrado(
            ClienteNoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PuntoEntregaNoEncontradoException.class)
    public ResponseEntity<Object> handlePuntoEntregaNoEncontrado(
            PuntoEntregaNoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<Object> handleUsuarioNoEncontrado(
            UsuarioNoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(RolNoPermitidoException.class)
    public ResponseEntity<Object> handleRolNoPermitido(
            RolNoPermitidoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(SkuYaExisteException.class)
    public ResponseEntity<Object> handleSkuYaExiste(
            SkuYaExisteException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<Object> handleProductoNoEncontrado(
            ProductoNoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(CategoriaNoEncontradaException.class)
    public ResponseEntity<Object> handleCategoriaNoEncontrada(
            CategoriaNoEncontradaException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InventarioNoEncontradoException.class)
    public ResponseEntity<Object> handleInventarioNoEncontrado(
            InventarioNoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(RolNoAutorizadoException.class)
    public ResponseEntity<Object> handleRolNoAutorizado(
            RolNoAutorizadoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

   

    @ExceptionHandler(MovimientoInvalidoException.class)
    public ResponseEntity<Object> handleMovimientoInvalido(
            MovimientoInvalidoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ResponseEntity<Object> handlePedidoNoEncontrado(
            PedidoNoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PedidoInvalidoException.class)
    public ResponseEntity<Object> handlePedidoInvalido(
            PedidoInvalidoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PedidoNoCancelableException.class)
    public ResponseEntity<Object> handlePedidoNoCancelable(
            PedidoNoCancelableException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PickingNoEncontradoException.class)
    public ResponseEntity<Object> handlePickingNoEncontrado(
            PickingNoEncontradoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PickingInvalidoException.class)
    public ResponseEntity<Object> handlePickingInvalido(
            PickingInvalidoException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    
    @ExceptionHandler(BultoInvalidoException.class)
    public ResponseEntity<Object> handleBultoInvalido(
        BultoInvalidoException ex,
        HttpServletRequest request) {

        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request
        );
    }



    @ExceptionHandler(BultoNoEncontradoException.class)
    public ResponseEntity<Object> handleBultoNoEncontrado(
        BultoNoEncontradoException ex,
        HttpServletRequest request) {

        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            ex.getMessage(),
            request
        );
    }

    @ExceptionHandler(DistribucionInvalidaException.class)
    public ResponseEntity<Object> handleDistribucionInvalida(
        DistribucionInvalidaException ex, HttpServletRequest request) {

        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request
        );
    }
    

















    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneric(
        Exception ex, HttpServletRequest request) {

            ex.printStackTrace();

        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Error interno del servidor",
            request
        );
    }

    private ResponseEntity<Object> buildErrorResponse(
            HttpStatus status, String message, HttpServletRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.name());
        body.put("message", message);
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }
}