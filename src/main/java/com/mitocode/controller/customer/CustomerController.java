package com.mitocode.controller.customer;

import com.mitocode.dto.request.customer.CustomerRequest;
import com.mitocode.exception.user.InvalidDocumentTypeException;
import com.mitocode.model.customer.Customer;
import com.mitocode.model.user.DocType;
import com.mitocode.service.customer.IConsultService;
import com.mitocode.service.customer.ICustomerService;
import com.mitocode.service.user.IDocTypeService;
import com.mitocode.util.MapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final MapperUtil mapperUtil;
    private final IConsultService service;
    private final ICustomerService customerService;
    private final IDocTypeService docTypeService;

    @GetMapping
    public ResponseEntity<List<CustomerRequest>> findAll() {
        List<CustomerRequest> list = mapperUtil.mapList(service.findAll(), CustomerRequest.class);
        //System.out.println("entregando CustomerDTO: " + list);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerRequest> findById(@PathVariable("id") Integer id) {
        Customer obj = service.findById(id);
        return ResponseEntity.ok(mapperUtil.map(obj, CustomerRequest.class));
    }


    @PostMapping
    public ResponseEntity<String> save(@Valid @RequestBody CustomerRequest dto) {
        System.out.println("Request Body: " + dto);


        // Buscar el tipo de documento usando el ID
        DocType documentType = docTypeService.findById(dto.getIdDoctype());
        if (documentType == null) {
            throw new InvalidDocumentTypeException("Tipo de documento no válido");
        }

        // Mapeo de CustomerDTO a Customer
        Customer customer = mapperUtil.map(dto, Customer.class);
        customer.setDocType(documentType);  // Asignar el tipo de documento al cliente

        // Guardar el cliente
        Customer savedCustomer = customerService.save(customer);

        // Crear la URI para la ubicación del nuevo recurso
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedCustomer.getIdCustomer())
                .toUri();

        // Devolver la respuesta con el código de estado 201 (Creado)
        return ResponseEntity.created(location).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerRequest> update(@Valid @PathVariable("id") Integer id, @RequestBody CustomerRequest dto) {
        dto.setIdCustomer(id);
        Customer obj = service.update(id, mapperUtil.map(dto, Customer.class));
        return ResponseEntity.ok(mapperUtil.map(obj, CustomerRequest.class));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}