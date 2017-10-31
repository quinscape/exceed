import React from "react"
import cx from "classnames"

class DocNav extends React.Component {

    render()
    {
        const { docs, active } = this.props;

        return (
            <ul className="doc-nav nav nav-pills nav-stacked te">
                {
                    docs.map( (doc,index) =>
                        <li key={doc.name} className={ cx(active === index && "active")}>
                            <a
                                className="btn btn-link"
                                href={ doc.name + ".html" }
                            >
                                <small>
                                { doc.title }
                                </small>
                            </a>
                        </li>
                    )
                }
            </ul>
        )
    }
}

export default DocNav
