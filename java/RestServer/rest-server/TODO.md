https://github.com/ptabasso2/springboot-mqtt


@Autowired
private MessagingService messagingService;
final String topic = "pejman/topic/event";

    messagingService.subscribe(topic);

    messagingService.publish(topic, "This is a sample message published to topic pejman/topic/event", 0, true);