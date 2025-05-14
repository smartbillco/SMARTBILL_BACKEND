package com.mitocode.controller.customer;

import com.mitocode.dto.request.customer.ConsultProcRequest;
import com.mitocode.model.customer.Customer;
import com.mitocode.model.MediaFile;
import com.mitocode.repo.user.IDocTypeRepo;
import com.mitocode.repo.user.IRegimeRepo;
import com.mitocode.service.customer.IConsultService;
import com.mitocode.service.customer.ICustomerService;
import com.mitocode.service.file.IMediaFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/consults")
@RequiredArgsConstructor
public class ConsultController {

    private final IConsultService service;
    private final IMediaFileService mfService;
    private final ICustomerService customerService;
    private final IRegimeRepo regimeRepository; // Declara el repositorio
    private final IDocTypeRepo docTypeRepository;

    @GetMapping("/callProcedureNative")
    public ResponseEntity<List<ConsultProcRequest>> callProcedureNative() {
        return ResponseEntity.ok(service.callProcedureOrFunctionNative());
    }

    @GetMapping(value = "/generateReport", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    //APPLICATION_PDF_VALUE o APPLICATION_OCTET_STREAM_VALUE
    public ResponseEntity<byte[]> generateReport() throws Exception {
        byte[] data = service.generateReport();

        return ResponseEntity.ok(data);
    }

    @GetMapping(value = "/readFile/{idFile}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> readFile(@PathVariable("idFile") Integer idFile) {
        byte[] data = mfService.findById(idFile).getContent();
        return ResponseEntity.ok(data);
    }

    @PostMapping(value = "/saveFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> saveFile(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("idCustomer") Integer idCustomer,
            @RequestParam("idRegime") Integer idRegime,
            @RequestParam("idDoctype") Integer idDoctype,  // Asegúrate de que está presente aquí
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("documentNumber") String documentNumber,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("address") String address,
            @RequestParam("email") String email,
            @RequestParam("dateOfBirth") String dateOfBirth) throws Exception {

        // Convertir la fecha de nacimiento de String a LocalDate si es necesario
        LocalDateTime dob = LocalDateTime.parse(dateOfBirth); // Cambia esto según el formato

        // Crear la entidad MediaFile
        MediaFile mf = new MediaFile();
        mf.setContent(multipartFile.getBytes());
        mf.setFileName(multipartFile.getOriginalFilename());
        mf.setFileType(multipartFile.getContentType());

        // Establecer los datos del cliente
        mf.setIdCustomer(idCustomer);

        mf.setRegime(regimeRepository.findById(idRegime).orElse(null)); // Asegúrate de tener acceso al repositorio
        mf.setDocType(docTypeRepository.findById(idDoctype).orElse(null)); // Asegúrate de tener acceso al repositorio
        mf.setFirstName(firstName);
        mf.setLastName(lastName);
        mf.setDocumentNumber(documentNumber);
        mf.setPhoneNumber(phoneNumber);
        mf.setAddress(address);
        mf.setEmail(email);
        mf.setDateOfBirth(dob);

        // Guardar en la base de datos
        mfService.save(mf);

        return ResponseEntity.ok().build();
    }

    private File convertToFile(MultipartFile multipartFile) throws Exception {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        FileOutputStream outputStream = new FileOutputStream(file);
        outputStream.write(multipartFile.getBytes());
        outputStream.close();
        return file;
    }


    @PostMapping(value = "/updateCustomerPhoto/{customerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateCustomerPhoto(@PathVariable("customerId") Integer customerId,
                                                      @RequestParam("photoFile") MultipartFile photoFile) throws Exception {

        // Verificar que el archivo no esté vacío
        if (photoFile.isEmpty()) {
            return ResponseEntity.badRequest().body("El archivo está vacío");
        }

        // Buscar al cliente por ID
        Customer customer = customerService.findById(customerId);

        if (customer != null) {
            try {
                // Convertir el contenido del archivo a base64
                //String base64Image = Base64.getEncoder().encodeToString(photoFile.getBytes());

                // Actualizar el campo photo_url del cliente con la imagen en base64
                //customer.setPhoto_url(base64Image);

                // Guardar los cambios en la base de datos
                customerService.update(customerId, customer);
                return ResponseEntity.ok("Foto actualizada con éxito");
            } catch (Exception e) {
                // Manejo del error
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al actualizar la foto");
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }


}