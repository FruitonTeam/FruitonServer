package cz.cuni.mff.fruiton.component;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import cz.cuni.mff.fruiton.annotation.HandleProtobufMessage;
import cz.cuni.mff.fruiton.dto.CommonProtos.ErrorMessage;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.service.communication.CommunicationService;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MessageDispatcher {

    private static final String[] SCAN_BASE_PACKAGES = {"cz.cuni.mff.fruiton.component"};

    private static final Logger logger = Logger.getLogger(MessageDispatcher.class.getName());

    private final ApplicationContext context;
    private final CommunicationService communicationService;

    private Map<Integer, DispatchMethod> methods = new HashMap<>();

    private Map<Integer, Descriptors.FieldDescriptor> fields = new HashMap<>();

    @Autowired
    public MessageDispatcher(final ApplicationContext context, final CommunicationService communicationService) {
        this.context = context;
        this.communicationService = communicationService;
    }

    @PostConstruct
    private void init() {

        List<Descriptors.OneofDescriptor> oneOfs = WrapperMessage.getDescriptor().getOneofs();

        Descriptors.OneofDescriptor oneOf = oneOfs.get(0);

        for (Descriptors.FieldDescriptor fd : oneOf.getFields()) {
            fields.put(fd.getNumber(), fd);
        }

        Set<Class<?>> scannedClasses = ReflectionUtils.getClassesInPackages(Arrays.asList(SCAN_BASE_PACKAGES));
        Set<Method> handleProtobufMessageMethods = ReflectionUtils.getMethodsWithAnnotation(
                scannedClasses, HandleProtobufMessage.class);

        for (Method m : handleProtobufMessageMethods) {

            HandleProtobufMessage handleProtobufMessage = m.getAnnotation(HandleProtobufMessage.class);
            WrapperMessage.MessageCase msgCase = handleProtobufMessage.messageCase();

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
    public final void dispatch(final WebSocketSession session, final BinaryMessage message) throws IOException {

        CodedInputStream cis = CodedInputStream.newInstance(message.getPayload());

        WrapperMessage msg = WrapperMessage.parseFrom(cis);

        int msgNum = msg.getMessageCase().getNumber();

        Object o = msg.getField(fields.get(msgNum));

        try {
            methods.get(msgNum).invoke(session.getPrincipal(), o);
        } catch (InvocationTargetException | IllegalAccessException e) {
            logger.log(Level.SEVERE, "Could not dispatch message", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception while processing WebSocket message", e);
            communicationService.send(session.getPrincipal(), createErrorMessage(e.getMessage()));
        }
    }

    private WrapperMessage createErrorMessage(final String message) {
        return WrapperMessage.newBuilder()
                .setErrorMessage(ErrorMessage.newBuilder().setMessage(message).build())
                .build();
    }

    private static class DispatchMethod {

        private Method m;
        private Object invocationObject;

        DispatchMethod(final Method m, final Object invocationObject) {
            this.m = m;
            this.invocationObject = invocationObject;
        }

        void invoke(final Object... args) throws InvocationTargetException, IllegalAccessException {
            m.invoke(invocationObject, args);
        }
    }


}
