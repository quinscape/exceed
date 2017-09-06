package de.quinscape.exceed.runtime.model;

import de.quinscape.exceed.model.TopLevelModel;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Is provided a context information to the expression transformation process. It encapsulates the toplevel model the
 * expression is associated with and, if applicable, a fine-detail object to further pinpoint the location.
 * (e.g. a component model in case of a component model expression)
 */
public final class ExpressionModelContext
{
    private final TopLevelModel topLevelModel;
    private final Object contextModel;
    private final Object fineDetail;

    public ExpressionModelContext(@NotNull TopLevelModel topLevelModel)
    {
        this(topLevelModel, null, null);
    }

    public ExpressionModelContext(@NotNull TopLevelModel topLevelModel, @NotNull Object contextModel)
    {
        this(topLevelModel, contextModel, null);
    }

    public ExpressionModelContext(@NotNull TopLevelModel topLevelModel, Object contextModel, Object fineDetail)
    {
        if (topLevelModel == null)
        {
            throw new IllegalArgumentException("topLevelModel can't be null");
        }
        this.topLevelModel = topLevelModel;
        this.contextModel = contextModel;
        this.fineDetail = fineDetail;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof ExpressionModelContext)
        {
            ExpressionModelContext that = (ExpressionModelContext) o;
            return
                Objects.equals(topLevelModel, that.topLevelModel) &&
                Objects.equals(contextModel, that.contextModel) &&
                Objects.equals(fineDetail, that.fineDetail);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(topLevelModel, contextModel, fineDetail);
    }



    public TopLevelModel getTopLevelModel()
    {
        return topLevelModel;
    }


    public Object getContextModel()
    {
        return contextModel;
    }


    public Object getFineDetail()
    {
        return fineDetail;
    }


    @Override
    public String toString()
    {
        return "ExpressionModelContext : "
            + "topLevelModel = " + topLevelModel 
            + ", contextModel = " + contextModel
            + ", fineDetail = " + fineDetail
            ;
    }
}
