# SK Happy Delivery

## 1. 분석/설계

### 이벤트스토밍

- url: http://www.msaez.io/#/storming/wf1WRjEyVVWd1Abldu2nsM6FwbL2/58c36eee763868e2a4b6cc1f019683fe


### 이벤트 도출

![이벤트도출결과](https://user-images.githubusercontent.com/45377807/125230473-ca215d00-e313-11eb-8866-2bdbfd5480be.png)




### 액터, 커맨드 부착
![액터 커맨드 부착](https://user-images.githubusercontent.com/45377807/125232032-c3e0b000-e316-11eb-8af4-cf98f7b97dac.png)
 

### 어그리게잇 묶기
![어그리게잇으로 묶기](https://user-images.githubusercontent.com/45377807/125232099-e7a3f600-e316-11eb-97ae-275b528205bd.png)


### 바운디드 컨텍스트 묶기
<img width="830" alt="바운디드컨텍스트 묶기" src="https://user-images.githubusercontent.com/45377807/125232137-f7bbd580-e316-11eb-82f0-293cd566faac.png">


### 폴리시 부착
<img width="828" alt="폴리시 추가" src="https://user-images.githubusercontent.com/45377807/125232200-16ba6780-e317-11eb-9e1d-db6cce330b92.png">


### 완성된 모형(실선은 Req/Res, 점선은 Pub/Sub)
<img width="830" alt="이벤트스토밍 결과" src="https://user-images.githubusercontent.com/45377807/125232289-423d5200-e317-11eb-83e9-f936ad5ea2c4.png">



### 헥사고날 아키텍처 다이어그램 도출
![헥사고날 아키텍쳐 다이어그램](https://user-images.githubusercontent.com/45377807/125297244-08456d80-e362-11eb-9164-e53cbfa52901.png)




## 2. 구현

분석/설계단계에서 도출된 헥사고날 아키넥처에 따라, 각 바운디트 컨텍스트 별로 대변되는 마이크로 서비스들을 스푸링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다

![메이븐 실행](https://user-images.githubusercontent.com/45377807/125234914-96970080-e31c-11eb-933b-7008c23038bf.png)


### Domain Driven Design의 적용
- 각 서비스 내에 도출된 핵심 어그리게잇 객체를 엔티티로 선언했다. 이때 가능한 현업에서 사용하는 유비쿼터스 랭귀지를 사용하려 노력했다.


      package skhappydelivery;
	
	  import javax.persistence.Entity;
	  import javax.persistence.GeneratedValue;
	  import javax.persistence.GenerationType;
	  import javax.persistence.Id;
	  import javax.persistence.PostPersist;
	  import javax.persistence.PostUpdate;
	  import javax.persistence.Table;
	  
	  import org.springframework.beans.BeanUtils;
	  
	  @Entity
	  @Table(name="Order_table")
	  public class Order {
	
    	@Id
    	@GeneratedValue(strategy=GenerationType.AUTO)
    	private Long orderId;
    	private Long customerId;
    	private String customerName;
    	private String customerAddress;
    	private Integer phoneNumber;
    	private Long menuId;
    	private Integer menuCount;
    	private Integer menuPrice;
    	private Long storeId;
    	private String orderStatus;  
	
    	
    	@PostPersist
    	public void onPostPersist(){
	
   	     skhappydelivery.external.Payed Payed = new skhappydelivery.external.Payed();
   	     // mappings goes here

 	       Payed.setCustomerId(this.customerId);
 	       Payed.setOrderId(this.orderId);
  	      Payed.setStoreId(this.storeId);
  	      Payed.setTotalPrice(this.menuCount * this.menuPrice);

 	       OrderApplication.applicationContext.getBean(skhappydelivery.external.PayService.class)
 	           .payed(Payed);
 	   }


	  @PostUpdate
 	  public void onPostUpdate(){
  	     OrderCanceled orderCanceled = new OrderCanceled();
	
		        //Reject >>> publish
				if(this.orderStatus=="orderCanceled"){

					BeanUtils.copyProperties(this, orderCanceled);

					orderCanceled.setOrderStatus(this.orderStatus);
			
					System.out.println(" PUBLISH orderCanceledOBJ:  " +orderCanceled.toString());
				
					orderCanceled.publishAfterCommit();
		
				}
	
    			}
	
		}
***

- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 
별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다


		OrderRepository.java
		
		package skhappydelivery;
			
		import org.springframework.data.repository.PagingAndSortingRepository;
		import org.springframework.data.rest.core.annotation.RepositoryRestResource;
			
		@RepositoryRestResource(collectionResourceRel="orders", path="orders")
		public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{
			
			
		}
***


#### kafka 활용한 Pub/Sub 구조

	package skhappydelivery;
	
	import java.util.Optional;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.cloud.stream.annotation.StreamListener;
	import org.springframework.messaging.handler.annotation.Payload;
	import org.springframework.stereotype.Service;
	
	import skhappydelivery.config.kafka.KafkaProcessor;
	
	@Service
	public class PolicyHandler{
	   @Autowired 
	   private PayRepository payRepository;
		
	   @Autowired
	   private PayService payService;
	
	   @StreamListener(KafkaProcessor.INPUT)
	   public void wheneverOrderCanceled_PayCancel(@Payload OrderCanceled orderCanceled){
	
	   if(!orderCanceled.validate()) return;
	        
	   System.out.println("\n\n##### listener PayCancel : " + orderCanceled.toString() + "\n\n");
	
	   try {
		Optional<Pay> tempObj =  payRepository.findById(orderCanceled.getOrderId());
	
		Pay payObj = new Pay();
	
		if(tempObj.isPresent()){
			payObj = tempObj.get();		
		}else{
			System.out.println("NO PAY data" );
		}
	
			payObj.setPayStatus("ORDERCANCELLED");
	
			payRepository.save(payObj);
	
			System.out.println(" PAYLIST data all :  " + payRepository.findAll().toString());
	
			System.out.println("ORDERCANCELLED SUCCESS");
				
		} catch (Exception e) {

	           System.out.println("\n\n##### listener PayCancel ERROR \n\n");
			
		}
	
	
	
	
	    }//wheneverOrderCanceled_PayCancel
	
	}


#### Correlation Key


#### Scaling-out(아래 HPA 참조)


#### 취소에 따른 보상 트랜젝션


#### CQRS


#### Message Consumer


엔티티 패턴과 레포지토리 패턴을 적용하여 JPA를 통한 다양한 데이터소스 유향에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST의 Repository를 적용했다.


### 적용 후 REST API의 테스트
![REST API테스트](https://user-images.githubusercontent.com/45377807/125294258-1645bf00-e35f-11eb-896e-31fb17193885.png)


### Polyglot Programming/Persistence
#### 
#### 
#### 



## 3. 운영

### SLA 준수
#### Pod생성 시 Liveness 와 Readiness Probe를 적용했는가?
#### 셀프힐링: Liveness Probe를 통해 일정 서비스 헬스 상태 저하에 따른 Pod 재생되는지 증명
<img width="1789" alt="Liveness Probe 수행" src="https://user-images.githubusercontent.com/45377807/125291419-59eaf980-e35c-11eb-90f4-edd1130c04c7.png">

#### 서킷브레이커 설정: 서킷브레이커 적용 + 리트라이 적용 + Pull Ejaction 적용
Istio 적용 예정


#### 오토스케일러(HPA)
<img width="962" alt="HPA(Autoscaling)_발췌" src="https://user-images.githubusercontent.com/45377807/125291395-5192be80-e35c-11eb-9a6a-a44c133427c8.png">


#### 모니터링, 앨러팅
#### Stateless 한 구현?



### CI/CD 설정
#### AWS Code Build 적용됐는가?
#### Contract Test
#### (Advanced) Canary Deploy, Shadow Deply, A/B Test (각 2점)
                
### 운영 유연성
#### Config Map / Secret



