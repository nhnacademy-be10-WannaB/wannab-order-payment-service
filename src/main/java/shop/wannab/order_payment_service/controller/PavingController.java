package shop.wannab.order_payment_service.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.order_payment_service.entity.Paving;
import shop.wannab.order_payment_service.entity.dto.PavingRequest;
import shop.wannab.order_payment_service.entity.dto.PavingResponse;
import shop.wannab.order_payment_service.service.PavingService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/paving")
public class PavingController {

    private final PavingService pavingService;

    /**
     * 생성
     */
    @PostMapping
    public ResponseEntity<PavingResponse> createPaving(@RequestBody @Valid PavingRequest request){

        Paving paving =  pavingService.createPaving(request);

        PavingResponse response = new PavingResponse(paving.getId(), paving.getName(), paving.getPrice());

        log.info("action=createPaving, name={}, price={}, message=\"포장 생성 요청 완료\"", request.getName(), request.getPrice());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    /**
     * 수정
     */
    @PostMapping("{paving-id}")
    public ResponseEntity<PavingResponse> updatePaving(@RequestBody @Valid PavingRequest request,
                                                       @PathVariable("paving-id") Long id){

        Paving paving = pavingService.updatePaving(id, request);

        PavingResponse response = new PavingResponse(paving.getId(), paving.getName(), paving.getPrice());
        log.info("action=updatePaving, id={}, name={}, price={}, message=\"포장 수정 요청 완료\"", id, request.getName(), request.getPrice());
        return ResponseEntity.ok(response);
    }

    /**
     * 삭제
     */
    @DeleteMapping("{paving-id}")
    public ResponseEntity<Void> deletePaving(@PathVariable("paving-id") Long id){

        pavingService.deletePaving(id);
        log.info("action=deletePaving, id={}, message=\"포장 삭제 요청 완료\"", id);
        return ResponseEntity.ok().build();
    }

    /**
     * 조회
     */
    @GetMapping
    public ResponseEntity<List<PavingResponse>> getPavingList(){
        List<PavingResponse> list = pavingService.getPavingList();
        log.info("action=getPavingList, message=\"포장 목록 조회 요청 완료\"");
        return ResponseEntity.ok(list);
    }

}