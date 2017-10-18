import React from "react"

import uri from "../../../util/uri"
import sys from "../../../sys"

class ResourceImage extends React.Component {

    render()
    {
        return (
            <img { ... this.props }
                 resource={ null }
                 src={
                     uri("/res/" + sys.appName + this.props.resource )
                 }
            />
        )
    }
}

export default ResourceImage
