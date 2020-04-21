package com.atguigu.gmall.payment;

import com.atguigu.gmall.util.ActiveMQUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.jms.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPaymentApplicationTests {

	@Autowired
	ActiveMQUtil activeMQUtil;

	@Test
	public void contextLoads() throws JMSException {

		ConnectionFactory connectionFactory = activeMQUtil.getConnectionFactory();

		Connection connection = connectionFactory.createConnection();
		connection.start();
		Session session = connection.createSession(true, Session.SESSION_TRANSACTED);// 开启消息事务

		// 队列
		Topic paymentResultQueue = session.createTopic("boss speak");

		//text文本格式，map键值格式
		TextMessage textMessage=new ActiveMQTextMessage();
		textMessage.setText("我们要为尚硅谷的伟大复兴而努力奋斗");
		MessageProducer producer = session.createProducer(paymentResultQueue);// 消息的生成者
		producer.send(textMessage);
		session.commit();
		producer.close();
		session.close();
		connection.close();
		;
	}

}
