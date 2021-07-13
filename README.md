# SK Happy Delivery

## 서비스 시나리오
### SK행복 배달 서비스

####기능적 요구사항
- 고객이 메뉴를 선택하여 주문한다
- 고객이 결제한다
- 결제가 되면 주문 및 결제 내역이 입점상점주인에게 전달된다
- 상점주인은 주문을 승인할 수 있다.
- 상점주인은 주문을 거절할 수 있다.
- 상점주인이 확인하여 주문을 접수하여 조리를 시작한다
- 고객이 주문을 취소할 수 있다
- 결제가 취소되면 상점에도 전달된다.
- 주문상태가 이벤트 발생시 마다 업데이트되어 View를 통해 보여진다

#### 비기능적 요구사항
##### 트랜잭션
- 주문이 되면 무조건 결제에 데이터가 저장된다. Sync 호출
- 상점주인이 주문을 승인하면 배달되어질 정보를 Order에서 가져와 저장된다.

##### 장애격리
- 상점관리 기능이 수행되지 않더라도 주문은 365일 24시간 받을 수 있어야 한다 Async (event-driven), Eventual Consistency
- 주문이 과도하게 생성되면 주문 생성을 잠시 지연하도록 유도한다 Circuit breaker, fallback

## 1. 분석/설계

### 이벤트스토밍

- url: http://www.msaez.io/#/storming/wf1WRjEyVVWd1Abldu2nsM6FwbL2/58c36eee763868e2a4b6cc1f019683fe


### 이벤트 도출

<img width="900" alt="이벤트도출" src="https://user-images.githubusercontent.com/45377807/125230473-ca215d00-e313-11eb-8866-2bdbfd5480be.png"><br/>




### 액터, 커맨드 부착
<img width="650" alt="액터 및 커맨드 부착" src="https://user-images.githubusercontent.com/45377807/125232032-c3e0b000-e316-11eb-8af4-cf98f7b97dac.png"><br/>
 

### 폴리시 부착
<img width="650" alt="폴리시 추가" src="https://user-images.githubusercontent.com/45377807/125399202-3ae77880-e3eb-11eb-9152-2f069f7f553f.png">



### 어그리게잇 묶기
<img width="650" alt="어그리게잇으로 묶기" src="https://user-images.githubusercontent.com/45377807/125399214-3de26900-e3eb-11eb-886f-465414fcf4db.png">


### 바운디드 컨텍스트 묶기
<img width="814" alt="바운디드컨텍스트 묶기" src="https://user-images.githubusercontent.com/45377807/125399252-4a66c180-e3eb-11eb-9d48-b31fda1d7227.png">



### 완성된 모형(실선은 Req/Res, 점선은 Pub/Sub)
<img width="847" alt="이벤트스토밍 결과" src="https://user-images.githubusercontent.com/45377807/125399275-518dcf80-e3eb-11eb-8092-221a4a7fb467.png">



### 헥사고날 아키텍처 다이어그램 도출
<img width="650" alt="헥사고날 아키텍쳐 다이어그램" src="https://user-images.githubusercontent.com/45377807/125297244-08456d80-e362-11eb-9164-e53cbfa52901.png"><br/>

<br/>
<br/>


## 2. 구현

분석/설계단계에서 도출된 헥사고날 아키넥처에 따라, 각 바운디트 컨텍스트 별로 대변되는 마이크로 서비스들을 스푸링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다

<img width="400" alt="메이븐 실행" src="https://user-images.githubusercontent.com/45377807/125234914-96970080-e31c-11eb-933b-7008c23038bf.png"><br/>


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






- Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 다양한 데이터소스 유형 (RDB or NoSQL) 에 대한 
별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST 의 RestRepository 를 적용하였다


		OrderRepository.java
		
		package skhappydelivery;
			
		import org.springframework.data.repository.PagingAndSortingRepository;
		import org.springframework.data.rest.core.annotation.RepositoryRestResource;
			
		@RepositoryRestResource(collectionResourceRel="orders", path="orders")
		public interface OrderRepository extends PagingAndSortingRepository<Order, Long>{
			
			
		}
<br/>
<br/>

### Req/Res 방식의 서비스 중심 아키텍쳐 구현
#### FeignClient 
- 결제 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현



		package skhappydelivery.external;
		
		import org.springframework.cloud.openfeign.FeignClient;
		import org.springframework.web.bind.annotation.RequestBody;
		import org.springframework.web.bind.annotation.RequestMapping;
		import org.springframework.web.bind.annotation.RequestMethod;
		
		@FeignClient(name="Pay", url="http://localhost:8085")
		public interface PayService {
		 
		    @RequestMapping(method= RequestMethod.GET, path="/pays")
		    public void pay(@RequestBody Pay pay);
		
		
		    @RequestMapping(method= RequestMethod.POST, path="/payed")
		    public void payed(@RequestBody Payed Payed);
		
		}


- 주문을 받은 직후(@PostPersist) 결제를 요청하도록 처리

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

#### Istio 구현
<img width="1000" alt="Istio 구현" src="https://user-images.githubusercontent.com/45377807/125317383-ee148b00-e373-11eb-981d-84a7ec4ce2e6.png"><br/>


### 이벤트 드리븐 아키텍쳐의 구현 
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

<img width="1000" alt="카프카 실행 증적" src="https://user-images.githubusercontent.com/45377807/125313408-2ca84680-e370-11eb-8828-40ea04e3240c.png"><br/>

#### 이벤트 드리븐 서비스 증적
##### 주문 생성
<img width="800" alt="오더 증적1" src="https://user-images.githubusercontent.com/45377807/125314385-1bac0500-e371-11eb-829e-feb8a4158772.png"><br/>
<img width="800" alt="오더 증적2" src="https://user-images.githubusercontent.com/45377807/125314416-21094f80-e371-11eb-9243-44502ac1928b.png"><br/>
##### 오더에 따른 결제 호출(Req/Res)
<img width="800" alt="결제 증적1" src="https://user-images.githubusercontent.com/45377807/125314434-26669a00-e371-11eb-8719-caa3e35fd054.png"><br/>
##### 결제 후 스토어에서 주문접수
<img width="800" alt="스토어오더접수 증적" src="https://user-images.githubusercontent.com/45377807/125314603-4eee9400-e371-11eb-9aa3-e3484943e402.png"><br/>
##### 고객이 주문취소(주문취소에 따른 스토어의 주문접수 취소)
##### 오더 정상생성 확인
<img width="800" alt="오더취소 증적1" src="https://user-images.githubusercontent.com/45377807/125314848-865d4080-e371-11eb-97e5-3b713334fef2.png"><br/>
<img width="800" alt="오더취소 증적2" src="https://user-images.githubusercontent.com/45377807/125314854-88270400-e371-11eb-90f5-ab4e83a581f2.png"><br/>
<img width="800" alt="오더취소 증적3" src="https://user-images.githubusercontent.com/45377807/125314857-89583100-e371-11eb-9418-7278eb213a75.png"><br/>
##### 접수된 주문에 대한 주문취소 수행
<img width="800" alt="오더취소 증적4" src="https://user-images.githubusercontent.com/45377807/125314867-8b21f480-e371-11eb-8c27-0980fc7818db.png"><br/>



#### Correlation Key
OrderService.java

	public Order getOrderService(StoreOrderAccepted storeOrderAcceptedObj) throws Exception {

		System.out.println("□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□ getOrderService start "+System.currentTimeMillis()+"□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□");


		try {
			Optional<Order> tempObj =  orderRepository.findById(storeOrderAcceptedObj.getOrderId());

			Order orderObj = new Order();

			if(tempObj.isPresent()){
				orderObj = tempObj.get();

				orderObj.setOrderStatus(storeOrderAcceptedObj.getOrderStatus());

				orderRepository.save(orderObj);
	
				return orderObj;		
			}else{
				return null ;
			}

		} catch (Exception e) {
			System.out.println("save Order Error" +e.getStackTrace());

			return null;
		}
	}

#### Scaling-out
<img width="1000" alt="HPA(Autoscaling)_발췌" src="https://user-images.githubusercontent.com/45377807/125291395-5192be80-e35c-11eb-9a6a-a44c133427c8.png"><br/>

#### 취소에 따른 보상 트랜젝션
##### 접수된 주문에 대한 주문취소 수행
<img width="800" alt="오더취소 증적4" src="https://user-images.githubusercontent.com/45377807/125314867-8b21f480-e371-11eb-8c27-0980fc7818db.png"><br/>

#### CQRS
<img width="500" alt="CQRS_1" src="https://user-images.githubusercontent.com/45377807/125406548-ea284d80-e3f3-11eb-9d08-08eb4c72bc33.png"><br/>
<img width="500" alt="CQRS_2" src="https://user-images.githubusercontent.com/45377807/125406570-eeed0180-e3f3-11eb-8e84-ec99f1e410fb.png"><br/>
<img width="500" alt="CQRS_3" src="https://user-images.githubusercontent.com/45377807/125406964-602cb480-e3f4-11eb-9c8a-fd7237e57268.png"><br/>




#### Message Consumer(kafka)
<img width="900" alt="카프카 실행 증적" src="https://user-images.githubusercontent.com/45377807/125395260-e988ba80-e3e5-11eb-998c-c587e2b7f9dd.png"><br/>



엔티티 패턴과 레포지토리 패턴을 적용하여 JPA를 통한 다양한 데이터소스 유향에 대한 별도의 처리가 없도록 데이터 접근 어댑터를 자동 생성하기 위하여 Spring Data REST의 Repository를 적용했다.

#### API Gateway
<img width="900" alt="gateway-2" src="https://user-images.githubusercontent.com/45377807/125408133-8868e300-e3f5-11eb-94a3-cd97329c5a54.JPG"><br/>


### 적용 후 REST API의 테스트
<img width="300" alt="REST API테스트" src="https://user-images.githubusercontent.com/45377807/125294258-1645bf00-e35f-11eb-896e-31fb17193885.png"><br/>



<br/>
<br/>
<br/> 



## 3. 운영

### SLA 준수
#### Pod생성 시 Liveness 와 Readiness Probe를 적용했는가?
##### Readiness Probe 적용
- order deployment.yml

		apiVersion: apps/v1
		kind: Deployment
		metadata:
		  name: order
		  labels:
		    app: order
		spec:
		  replicas: 1
		  selector:
		    matchLabels:
		     app: order
		  template:
		    metadata:
		      labels:
		       app: order
		    spec:
		      containers:
		       - name: order
			 image: 879772956301.dkr.ecr.ap-southeast-2.amazonaws.com/user05-order:v2
			  args:
			 - /bin/sh
			  - -c
			  - touch /tmp/healthy; sleep 30; rm -rf /tmp/healthy; sleep 600
			ports:
			  - containerPort: 8080
			  readinessProbe:
			    exec:
			      command:
			      - cat
			      - /tmp/healthy
			    initialDelaySeconds: 10
			    timeoutSeconds: 2
			    periodSeconds: 5
			    failureThreshold: 10
			  livenessProbe:
			    exec:
			      command:
			      - cat
			      - /tmp/healthy
			    initialDelaySeconds: 120
			    timeoutSeconds: 2
			    periodSeconds: 5
			    failureThreshold: 5
			  volumeMounts:
			  - mountPath: "/mnt/aws"
			    name: volume
		      volumes:
			  - name: volume
			    persistentVolumeClaim:
			      claimName: aws-efs




#### 셀프힐링: Liveness Probe 
<img width="1000" alt="Liveness Probe 수행" src="https://user-images.githubusercontent.com/45377807/125291419-59eaf980-e35c-11eb-90f4-edd1130c04c7.png"><br/>

#### 서킷브레이커 설정
##### OrderService.java

	@Transactional
		public Order getOrderService(StoreOrderAccepted storeOrderAcceptedObj) throws Exception {

			System.out.println("□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□ getOrderService start "+System.currentTimeMillis()+"□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□");

			try {
				Optional<Order> tempObj =  orderRepository.findById(storeOrderAcceptedObj.getOrderId());

				Order orderObj = new Order();

		    try {
		    System.out.println("\n\n\n\n\n\nWAITINGWAITINGWAITINGWAITINGWAITINGWAITINGWAITINGWAITINGWAITINGWAITINGWAITINGWAITINGWAITING");

		       Thread.currentThread().sleep((long) (400 + Math.random() * 220));
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }

				if(tempObj.isPresent()){
					orderObj = tempObj.get();

					orderObj.setOrderStatus(storeOrderAcceptedObj.getOrderStatus());

					orderRepository.save(orderObj);

					return orderObj;		
				}else{
					return null ;
				}

			} catch (Exception e) {
				System.out.println("save Order Error" +e.getStackTrace());

				return null;
			}
		}

	 }//classOrderPlacedService

	Store

	Application.yml

	feign:
	  hystrix:
	    enabled: true

	# To set thread isolation to SEMAPHORE
	#hystrix:
	#  command:
	#    default:
	#      execution:
	#        isolation:
	#          strategy: SEMAPHORE

	hystrix:
	  command:
	    # 전역설정
	    default:
	      execution.isolation.thread.timeoutInMilliseconds: 610


<img width="1000" alt="써킷브레이커-1" src="https://user-images.githubusercontent.com/45377807/125397429-e3480d80-e3e8-11eb-8e8c-e49329bcf9b9.JPG"><br/>


#### 오토스케일러(HPA)
<img width="1000" alt="HPA(Autoscaling)_발췌" src="https://user-images.githubusercontent.com/45377807/125291395-5192be80-e35c-11eb-9a6a-a44c133427c8.png"><br/>


#### 모니터링, 앨러팅
##### Kiali
<img width="800" alt="모니터링_kiali" src="https://user-images.githubusercontent.com/45377807/125376611-5e4bfc80-e3c6-11eb-97e9-1b83e68d207e.png"><br/>


##### Jaeger
<img width="800" alt="모니터링_예거" src="https://user-images.githubusercontent.com/45377807/125376614-6015c000-e3c6-11eb-8112-deb54075ba48.png"><br/>



### CI/CD 설정
#### AWS Code Build 적용됐는가?

##### buildspec-kubectl.yaml 파일
<img width="400" alt="빌드스펙yaml파일" src="https://user-images.githubusercontent.com/45377807/125326441-02a95100-e37d-11eb-8db8-1130577a0cff.png"><br/>

##### 빌드 성공
<img width="800" alt="코드빌드1" src="https://user-images.githubusercontent.com/45377807/125326080-9e868d00-e37c-11eb-9cdb-093edb64efaf.png"><br/>
<img width="800" alt="코드빌드2" src="https://user-images.githubusercontent.com/45377807/125326094-a0e8e700-e37c-11eb-8263-0babce52cb25.png"><br/>

                
### 운영 유연성
#### Config Map / Secret
ConfigMap은 Persistent Volume 으로 구현
<img width="1367" alt="PV 할당" src="https://user-images.githubusercontent.com/45377807/125376098-3e680900-e3c5-11eb-909d-79f359a9fa57.png"><br/>
<img width="1366" alt="pod 내 volume 마운트" src="https://user-images.githubusercontent.com/45377807/125376100-4162f980-e3c5-11eb-9585-9f01a8afe4ed.png"><br/>






