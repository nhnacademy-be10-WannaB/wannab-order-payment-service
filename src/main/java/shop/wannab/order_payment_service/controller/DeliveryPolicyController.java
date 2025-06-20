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
import shop.wannab.order_payment_service.entity.DeliveryPolicy;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyRequest;
import shop.wannab.order_payment_service.entity.dto.DeliveryPolicyResponse;
import shop.wannab.order_payment_service.service.DeliveryPolicyService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/delivery-policy")
public class DeliveryPolicyController {

    private final DeliveryPolicyService deliveryPolicyService;


    //생성
    @PostMapping
    public ResponseEntity<DeliveryPolicyResponse> createDeliveryPolicy(@RequestBody @Valid DeliveryPolicyRequest request){

        DeliveryPolicy dp = deliveryPolicyService.createDeliveryPolicy(request);

        //반환하기위해 response에 담음
        DeliveryPolicyResponse response = new DeliveryPolicyResponse(dp.getId(), dp.getName(), dp.getMinPrice(), dp.getFee());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    //수정
    @PutMapping("{dp-id}")
    public ResponseEntity<DeliveryPolicyResponse> updateDeliveryPolicy(@RequestBody @Valid DeliveryPolicyRequest request,
                                                                       @PathVariable("dp-id") Long id){
        DeliveryPolicy dp = deliveryPolicyService.updateDeliveryPolicy(id, request);

        //반환하기위해 response에 담음
        DeliveryPolicyResponse response = new DeliveryPolicyResponse(dp.getId(), dp.getName(), dp.getMinPrice(), dp.getFee());
        return ResponseEntity.ok(response);
    }

    //삭제
    @DeleteMapping("{dp-id}")
    public ResponseEntity<Void> deleteDeliverPolicy(@PathVariable("dp-id") Long id){

        deliveryPolicyService.deleteDeliveryPolicy(id);
        return ResponseEntity.ok().build();
    }

    //전체목록조회
    @GetMapping
    public ResponseEntity<List<DeliveryPolicyResponse>> getDeliveryPolicyList(){
        List<DeliveryPolicyResponse> list = deliveryPolicyService.getDeliveryPolicyList();
        return ResponseEntity.ok(list);
    }
}
