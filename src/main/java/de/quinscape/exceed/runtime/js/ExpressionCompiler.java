package de.quinscape.exceed.runtime.js;

import de.quinscape.exceed.model.ApplicationModel;

public interface ExpressionCompiler
{
    ExpressionBundle compile(ApplicationModel applicationModel);
}
