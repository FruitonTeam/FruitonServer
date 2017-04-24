package cz.cuni.mff.fruiton.component;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dto.UserProtos;
import cz.cuni.mff.fruiton.util.ReflectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MessageDispatcher {

    private static final String[] SCAN_BASE_PACKAGES = {"cz.cuni.mff.fruiton.component"};

    private static final Logger logger = Logger.getLogger(MessageDispatcher.class.getName());

    private ApplicationContext context;

    private Map<Integer, DispatchMethod> methods = new HashMap<>();

    private Map<Integer, Descriptors.FieldDescriptor> fields = new HashMap<>();

    @Autowired
    public MessageDispatcher(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    private void init() {

        List<Descriptors.OneofDescriptor> oneOfs = UserProtos.WrapperMessage.getDescriptor().getOneofs();

        Descriptors.OneofDescriptor oneOf = oneOfs.get(0);

        for (Descriptors.FieldDescriptor fd : oneOf.getFields()) {
            fields.put(fd.getNumber(), fd);
        }

        Set<Class<?>> scannedClasses = ReflectionUtils.getClassesInPackages(Arrays.asList(SCAN_BASE_PACKAGES));
        Set<Method> handleProtobufMessageMethods = ReflectionUtils.getMethodsWithAnnotation(
                scannedClasses, HandleProtobufMessage.class);

        for (Method m : handleProtobufMessageMethods) {

            HandleProtobufMessage handleProtobufMessage = m.getAnnotation(HandleProtobufMessage.class);
            UserProtos.WrapperMessage.MsgCase msgCase = handleProtobufMessage.msgCase();

            int msgNumber = msgCase.getNumber();

            methods.put(msgNumber, new DispatchMethod(m, context.getBean(m.getDeclaringClass())));

        }
    }

    /**
     * Dispatches message to correct handlers.
     * @param session session for the user who send the message
     * @param message binary message to dispatch
     * @throws IOException if message could not be converted to protobuf
     */
    public void dispatch(WebSocketSession session, BinaryMessage message) throws IOException {

        CodedInputStream cis = CodedInputStream.newInstance(message.getPayload());

        UserProtos.WrapperMessage msg = UserProtos.WrapperMessage.parseFrom(cis);

        int msgNum = msg.getMsgCase().getNumber();

        Object o = msg.getField(fields.get(msgNum));

        try {
            methods.get(msgNum).invoke(session.getPrincipal(), o);
        } catch (InvocationTargetException|IllegalAccessException e) {
            logger.log(Level.SEVERE, "Could not dispatch message", e);
        }
    }

    private static class DispatchMethod {

        private Method m;
        private Object invocationObject;

        public DispatchMethod(Method m, Object invocationObject) {
            this.m = m;
            this.invocationObject = invocationObject;
        }

        public void invoke(Object... args) throws InvocationTargetException, IllegalAccessException {
            m.invoke(invocationObject, args);
        }
    }


}
