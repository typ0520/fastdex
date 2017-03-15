package bizsocket.core.internal;

import bizsocket.core.RequestContext;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by tong on 16/10/5.
 */
public class RequestContextQueue extends CopyOnWriteArrayList<RequestContext> {
    @Override
    public boolean remove(Object o) {
        if (o != null) {
            RequestContext requestContext = (RequestContext) o;
            beforeRemove(requestContext);
        }
        return super.remove(o);
    }

    @Override
    public RequestContext remove(int index) {
        RequestContext requestContext = super.remove(index);
        beforeRemove(requestContext);
        return requestContext;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        Collection<RequestContext> collection = (Collection<RequestContext>) c;
        for (RequestContext context : collection) {
            beforeRemove(context);
        }
        return super.removeAll(c);
    }

    @Override
    public void clear() {
        for (RequestContext context : this) {
            beforeRemove(context);
        }
        super.clear();
    }

    @Override
    public boolean add(RequestContext requestContext) {
        beforeAdd(requestContext);
        return super.add(requestContext);
    }

    @Override
    public void add(int index, RequestContext element) {
        beforeAdd(element);
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends RequestContext> c) {
        Collection<RequestContext> collection = (Collection<RequestContext>) c;
        for (RequestContext context : collection) {
            beforeAdd(context);
        }
        return super.addAll(c);
    }

    private void beforeAdd(RequestContext requestContext) {
        if (requestContext != null) {
            if (!contains(requestContext)) {
                requestContext.onAddToQueue();
            }
        }
    }

    private void beforeRemove(RequestContext requestContext) {
        if (requestContext != null) {
            requestContext.onRemoveFromQueue();
        }
    }
}
