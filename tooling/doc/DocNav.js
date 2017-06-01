import React from "react"

class DocNav extends React.Component {

    render()
    {
        const { docs } = this.props;

        return (
            <ul className="doc-nav">
                {
                    docs.map( doc =>
                        <li key={doc.name}>
                            <a
                                className="btn btn-link"
                                href={ doc.name + ".html" }
                            >
                               { doc.title }
                            </a>
                        </li>
                    )
                }
            </ul>
        )
    }
}

export default DocNav
