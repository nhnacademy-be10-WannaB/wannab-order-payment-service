package shop.wannab.order_payment_service.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.wannab.order_payment_service.entity.WrappingPaper;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperRequest;
import shop.wannab.order_payment_service.entity.dto.WrappingPaperResponse;
import shop.wannab.order_payment_service.service.WrappingPaperService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/wrapping-papers")
public class WrappingPaperController {

    private final WrappingPaperService wrappingPaperService;

    //포장지 생성
    @PostMapping
    public ResponseEntity<WrappingPaperResponse> createWrappingPaper(@RequestBody @Valid WrappingPaperRequest request){
        WrappingPaper wrappingPaper = wrappingPaperService.creatWrappingPaper(request);

        //생성된 포장지 반환하기위해
        WrappingPaperResponse response = new WrappingPaperResponse(wrappingPaper.getId(), wrappingPaper.getName(), wrappingPaper.getPrice());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //포장지 수정
    @PutMapping("{wp-id}")
    public ResponseEntity<WrappingPaperResponse> updateWrappingPaper(@PathVariable("wp-id") Long id,
                                                                     @RequestBody @Valid WrappingPaperRequest request){
        WrappingPaper wrappingPaper = wrappingPaperService.updateWrappingPaper(id, request);

        //업데이트된 포장지 반환
        WrappingPaperResponse response = new WrappingPaperResponse(wrappingPaper.getId(), wrappingPaper.getName(), wrappingPaper.getPrice());
        return ResponseEntity.ok(response);
    }

    //포장지 삭제
    @DeleteMapping("{wp-id}")
    public ResponseEntity<Void> deleteWrappingPaper(@PathVariable("wp-id") Long id){
        wrappingPaperService.deleteWrappingPaper(id);
        return ResponseEntity.ok().build();
    }


    //포장지조회
    @GetMapping
    public ResponseEntity<List<WrappingPaperResponse>> getWrappingPaperList(){
        List<WrappingPaperResponse> list = wrappingPaperService.getWrappingPaperList();
        return ResponseEntity.ok(list);
    }
}
