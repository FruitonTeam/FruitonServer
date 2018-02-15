package cz.cuni.mff.fruiton.component;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.Descriptors;
import cz.cuni.mff.fruiton.annotation.ProtobufMessage;
import cz.cuni.mff.fruiton.dto.CommonProtos.ErrorMessage;
import cz.cuni.mff.fruiton.dto.CommonProtos.ErrorMessage.ErrorId;
import cz.cuni.mff.fruiton.dto.CommonProtos.WrapperMessage;
import cz.cuni.mff.fruiton.exception.FruitonServerException;
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
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class MessageDispatcher {

    private static final List<String> SCAN_BASE_PACKAGES = List.of(
            "cz.cuni.mff.fruiton.component",
            "cz.cuni.mff.fruiton.service"
    );

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

        Set<Class<?>> scannedClasses = ReflectionUtils.getClassesInPackages(SCAN_BASE_PACKAGES, context.getEnvironment());
        Set<Method> handleProtobufMessageMethods = ReflectionUtils.getMethodsWithAnnotation(
                scannedClasses, ProtobufMessage.class);

        for (Method m : handleProtobufMessageMethods) {

            ProtobufMessage protobufMessage = m.getAnnotation(ProtobufMessage.class);
            WrapperMessage.MessageCase msgCase = protobufMessage.messageCase();

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
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Could not dispatch message", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            logger.log(Level.WARNING, "Exception while processing WebSocket message", cause);
            communicationService.send(session.getPrincipal(), createErrorMessage(cause));
        }
    }

    private ErrorId getErrorId(final Throwable throwable) {
        if (throwable instanceof FruitonServerException) {
            return ((FruitonServerException) throwable).getErrorId();
        }
        return ErrorId.GENERAL;
    }

    private WrapperMessage createErrorMessage(final Throwable throwable) {
        return WrapperMessage.newBuilder()
                .setErrorMessage(ErrorMessage.newBuilder()
                        .setMessage(throwable.getMessage())
                        .setErrorId(getErrorId(throwable)))
                .build();
    }

    private static class DispatchMethod {

        private final Method m;
        private final Object invocationObject;

        private boolean passMessage = false;

        DispatchMethod(final Method m, final Object invocationObject) {
            this.m = m;
            this.invocationObject = invocationObject;

            if (!m.canAccess(invocationObject)) {
                if (!m.trySetAccessible()) {
                    throw new InternalError("Cannot access given method " + m);
                }
            }

            if (m.getParameterCount() > 1) {
                passMessage = true;
            }
        }

        void invoke(final Principal principal, final Object msg) throws InvocationTargetException, IllegalAccessException {
            if (passMessage) {
                m.invoke(invocationObject, principal, msg);
            } else {
                m.invoke(invocationObject, principal);
            }
        }
    }

}
