package com.aredondocharro.ClothingStore.sharedTEST.infrastructure.event;

import com.aredondocharro.ClothingStore.shared.domain.event.DomainEvent;
import com.aredondocharro.ClothingStore.shared.infrastructure.event.SpringEventBusAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.mockito.Mockito.*;

class SpringEventBusAdapterTest {

    @Test
    void publish_delegatesToApplicationEventPublisher() {
        ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
        SpringEventBusAdapter adapter = new SpringEventBusAdapter(publisher);

        DomainEvent evt = mock(DomainEvent.class);
        when(evt.type()).thenReturn("test.event");

        adapter.publish(evt);

        verify(publisher, times(1)).publishEvent(evt);
        verifyNoMoreInteractions(publisher);
    }
}
